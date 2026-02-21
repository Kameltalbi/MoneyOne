# Add project specific ProGuard rules here.

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes used by Room
-keep class com.smartbudget.data.entity.** { *; }
-keep class com.smartbudget.data.dao.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Google Play Billing
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# Google Drive API
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.drive.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ViewModels - CRITICAL for app to work
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}
-keep class com.smartbudget.ui.viewmodel.** { *; }
-keepclassmembers class com.smartbudget.ui.viewmodel.** {
    <init>(...);
}

# ViewModel Factory
-keep class androidx.lifecycle.ViewModelProvider$Factory { *; }
-keep class * implements androidx.lifecycle.ViewModelProvider$Factory { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class kotlin.Metadata { *; }

# Navigation
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep all classes in our package - NO OBFUSCATION
-keep,allowobfuscation class com.smartbudget.** { *; }
-keepclassmembers class com.smartbudget.** { *; }
-keepnames class com.smartbudget.** { *; }
-dontobfuscate
