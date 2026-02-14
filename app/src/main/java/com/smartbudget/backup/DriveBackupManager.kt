package com.smartbudget.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

class DriveBackupManager(private val context: Context) {

    companion object {
        private const val BACKUP_FOLDER = "MoneyOne_Backup"
        private const val DB_NAME = "smartbudget_database"
    }

    private val prefs = context.getSharedPreferences("drive_backup", Context.MODE_PRIVATE)

    fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent = getSignInClient().signInIntent

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            // Persist email when we detect a valid account
            prefs.edit().putString("account_email", account.email).putBoolean("signed_in", true).apply()
            return true
        }
        return prefs.getBoolean("signed_in", false)
    }

    fun getAccountEmail(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.email
            ?: prefs.getString("account_email", null)
    }

    suspend fun silentSignIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            val task = getSignInClient().silentSignIn()
            if (task.isSuccessful) {
                val account = task.result
                prefs.edit().putString("account_email", account?.email).putBoolean("signed_in", true).apply()
                true
            } else {
                com.google.android.gms.tasks.Tasks.await(task)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun markSignedOut() {
        prefs.edit().putBoolean("signed_in", false).remove("account_email").apply()
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("MoneyOne").build()
    }

    private suspend fun getOrCreateFolder(driveService: Drive): String = withContext(Dispatchers.IO) {
        // Check if folder exists in appDataFolder
        val result = driveService.files().list()
            .setQ("name='$BACKUP_FOLDER' and mimeType='application/vnd.google-apps.folder' and trashed=false")
            .setSpaces("appDataFolder")
            .execute()

        if (result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            val folderMetadata = com.google.api.services.drive.model.File().apply {
                name = BACKUP_FOLDER
                mimeType = "application/vnd.google-apps.folder"
                parents = listOf("appDataFolder")
            }
            driveService.files().create(folderMetadata)
                .setFields("id")
                .execute().id
        }
    }

    suspend fun backup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Not signed in"))

            val driveService = getDriveService(account)
            val folderId = getOrCreateFolder(driveService)

            // Close DB connections before copying
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database not found"))
            }

            // Copy DB to cache for upload
            val tempFile = File(context.cacheDir, "backup_$DB_NAME")
            dbFile.copyTo(tempFile, overwrite = true)

            // Also copy WAL and SHM if they exist
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")

            // Delete existing backup files in Drive
            val existing = driveService.files().list()
                .setQ("'$folderId' in parents and name='$DB_NAME' and trashed=false")
                .setSpaces("appDataFolder")
                .execute()
            for (f in existing.files) {
                driveService.files().delete(f.id).execute()
            }

            // Upload DB
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = DB_NAME
                parents = listOf(folderId)
            }
            val mediaContent = FileContent("application/octet-stream", tempFile)
            driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()

            tempFile.delete()

            // Save backup timestamp
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_backup_time", System.currentTimeMillis())
                .apply()

            Result.success(account.email ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restore(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Not signed in"))

            val driveService = getDriveService(account)
            val folderId = getOrCreateFolder(driveService)

            // Find backup file
            val result = driveService.files().list()
                .setQ("'$folderId' in parents and name='$DB_NAME' and trashed=false")
                .setSpaces("appDataFolder")
                .execute()

            if (result.files.isEmpty()) {
                return@withContext Result.failure(Exception("No backup found"))
            }

            val backupFileId = result.files[0].id

            // Download to temp
            val tempFile = File(context.cacheDir, "restore_$DB_NAME")
            val outputStream = FileOutputStream(tempFile)
            driveService.files().get(backupFileId).executeMediaAndDownloadTo(outputStream)
            outputStream.close()

            // Replace DB
            val dbFile = context.getDatabasePath(DB_NAME)
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")

            // Delete WAL/SHM
            walFile.delete()
            shmFile.delete()

            // Copy restored file
            tempFile.copyTo(dbFile, overwrite = true)
            tempFile.delete()

            Result.success("OK")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            getSignInClient().signOut()
            markSignedOut()
        }
    }
}
