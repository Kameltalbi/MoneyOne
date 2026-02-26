package com.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.SmartBudgetApp
import com.smartbudget.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(
    onNavigateBack: () -> Unit,
    onNavigateProUpgrade: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as SmartBudgetApp
    val isPro by app.billingManager.isPro.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fonctionnalités",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // App summary
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MoneyOne",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Votre assistant de gestion financière personnelle",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (isPro) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = IncomeGreen
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Membre Pro",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Free Features Section
            Text(
                text = "✨ Fonctionnalités Gratuites",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            FeatureCard(
                icon = Icons.Filled.AccountBalanceWallet,
                title = "Gestion de compte unique",
                description = "Suivez vos revenus et dépenses sur un compte principal.",
                isFree = true
            )

            FeatureCard(
                icon = Icons.Filled.Category,
                title = "Catégories personnalisées",
                description = "Créez et gérez vos propres catégories de transactions avec icônes et couleurs.",
                isFree = true
            )

            FeatureCard(
                icon = Icons.Filled.TrendingUp,
                title = "Budget global mensuel",
                description = "Définissez un budget mensuel et suivez vos dépenses en temps réel.",
                isFree = true
            )

            FeatureCard(
                icon = Icons.Filled.BarChart,
                title = "Statistiques de base",
                description = "Visualisez vos revenus, dépenses et solde du mois en cours.",
                isFree = true
            )

            FeatureCard(
                icon = Icons.Filled.Repeat,
                title = "Transactions récurrentes",
                description = "Configurez des transactions automatiques (salaire, loyer, abonnements).",
                isFree = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pro Features Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Fonctionnalités Pro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isPro) {
                        Button(
                            onClick = onNavigateProUpgrade,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Passer Pro", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            FeatureCard(
                icon = Icons.Filled.BarChart,
                title = "Dashboard complet",
                description = "Graphiques avancés : évolution du solde, dépenses par catégorie, comparaison mensuelle.",
                isFree = false,
                isPro = isPro
            )

            FeatureCard(
                icon = Icons.Filled.AccountBalance,
                title = "Multi-comptes (jusqu'à 5)",
                description = "Gérez plusieurs comptes bancaires, portefeuilles et cartes en même temps.",
                isFree = false,
                isPro = isPro
            )

            FeatureCard(
                icon = Icons.Filled.Label,
                title = "Budgets par catégorie",
                description = "Définissez des budgets spécifiques pour chaque catégorie de dépenses.",
                isFree = false,
                isPro = isPro
            )

            FeatureCard(
                icon = Icons.Filled.Layers,
                title = "Consolidation multi-comptes",
                description = "Vue consolidée de tous vos comptes avec solde total et statistiques globales.",
                isFree = false,
                isPro = isPro
            )

            FeatureCard(
                icon = Icons.Filled.FileDownload,
                title = "Export CSV & PDF",
                description = "Exportez vos transactions en format CSV ou PDF pour analyse externe.",
                isFree = false,
                isPro = isPro
            )

            FeatureCard(
                icon = Icons.Filled.Lock,
                title = "Sécurité PIN & Empreinte",
                description = "Protégez vos données avec un code PIN ou l'authentification biométrique.",
                isFree = false,
                isPro = isPro
            )

            FeatureCard(
                icon = Icons.Filled.Language,
                title = "Multi-devises avec conversion auto",
                description = "Créez des comptes en USD, EUR, MAD, etc. Les montants sont automatiquement convertis dans votre devise principale avec les taux de change en temps réel.",
                isFree = false,
                isPro = isPro
            )

            FeatureCard(
                icon = Icons.Filled.Message,
                title = "Import SMS bancaires",
                description = "Importez automatiquement vos transactions depuis vos SMS bancaires.",
                isFree = false,
                isPro = isPro
            )

            FeatureCard(
                icon = Icons.Filled.CloudUpload,
                title = "Sauvegarde Google Drive",
                description = "Sauvegardez et restaurez vos données sur Google Drive automatiquement.",
                isFree = false,
                isPro = isPro
            )

            FeatureCard(
                icon = Icons.Filled.Sync,
                title = "Synchro cloud",
                description = "Synchronisez vos données entre plusieurs appareils automatiquement.",
                isFree = false,
                isPro = isPro
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pro upgrade link for all users
            Card(
                onClick = onNavigateProUpgrade,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPro) 
                        MaterialTheme.colorScheme.surfaceVariant
                    else 
                        Color(0xFFFFD700).copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPro) "Vous êtes membre Pro" else "Passer à la version Pro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPro) 
                                "Merci pour votre soutien !"
                            else 
                                "Débloquez toutes les fonctionnalités premium",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy policy link
            TextButton(
                onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://sites.google.com/view/moneyone-app/accueil")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.PrivacyTip,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Politique de confidentialité")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    isFree: Boolean,
    isPro: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFree) 
                MaterialTheme.colorScheme.surface
            else if (isPro)
                IncomeGreen.copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFree) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else if (isPro) IncomeGreen.copy(alpha = 0.15f)
                        else Color.Gray.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isFree) MaterialTheme.colorScheme.primary
                           else if (isPro) IncomeGreen
                           else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!isFree && !isPro) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    if (!isFree && isPro) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = IncomeGreen
                        ) {
                            Text(
                                text = "✓",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    if (!isFree && !isPro) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFD700)
                        ) {
                            Text(
                                text = "PRO",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!isFree && !isPro) 
                        Color.Gray 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
