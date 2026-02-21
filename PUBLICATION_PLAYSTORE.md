# 🚀 CHECKLIST PUBLICATION PLAY STORE - MoneyOne

**Date de publication prévue** : 21 Février 2026

---

## ✅ FICHIERS PRÊTS (100% Complété)

### 📦 App Bundle
- **Fichier** : `app-release.aab` (38 MB)
- **Localisation** : `/Users/kameltalbi/Repos2/MoneyOne/app/build/outputs/bundle/release/`
- **Version** : 1.1 (versionCode: 2)
- **Status** : ✅ Signé et prêt
- **Mode** : Production (isPro = false par défaut)

### 📄 Documents
- **Privacy Policy** : `PRIVACY_POLICY.md` ✅ Créée

---

## 🔴 À FAIRE DEMAIN MATIN (Obligatoire)

### 1. Héberger la Privacy Policy (15 min)

**Option rapide - GitHub Pages** :
1. Créer un repo public "moneyone-privacy"
2. Uploader PRIVACY_POLICY.md renommé en index.md
3. Activer GitHub Pages dans Settings
4. URL finale: https://votre-username.github.io/moneyone-privacy

**OU Firebase Hosting** :
```bash
cd /Users/kameltalbi/Repos2/MoneyOne
firebase init hosting
# Copier PRIVACY_POLICY.md vers public/index.html
firebase deploy
```

### 2. Créer les Screenshots (30 min)

**Minimum requis** :
- 2 screenshots Phone (1080x2340 px)
- Recommandé : 5-6 écrans différents

**Écrans à capturer** :
1. 📱 Accueil (calendrier avec transactions)
2. 💰 Ajout de transaction
3. 📊 Écran Budgets
4. 🎯 Objectifs d'épargne
5. ⚙️ Settings
6. 📈 Dashboard (optionnel)

**Astuce** : Utilisez l'émulateur, prenez des screenshots, puis redimensionnez à 1080x2340

### 3. Créer Feature Graphic (20 min)

- **Taille** : 1024x500 pixels
- **Outil** : Canva (gratuit) ou Figma
- **Contenu** : Logo MoneyOne + slogan "Gérez votre budget simplement"

---

## 📝 DANS PLAY CONSOLE (1-2h)

### Étape 1 : Créer l'application
1. Aller sur https://play.google.com/console
2. Cliquer "Créer une application"
3. Nom : **MoneyOne**
4. Langue par défaut : **Français**

### Étape 2 : Fiche du Store

**Titre** (30 caractères max) :
```
MoneyOne - Budget & Épargne
```

**Description courte** (80 caractères) :
```
Gérez budget, dépenses et objectifs d'épargne. Simple, sécurisé, sans pub.
```

**Description complète** :
```
💰 MoneyOne - Votre Assistant Budget Personnel

Prenez le contrôle de vos finances avec MoneyOne, l'application de gestion budgétaire simple et puissante.

✨ FONCTIONNALITÉS PRINCIPALES

📊 Suivi des Transactions
• Ajoutez vos revenus et dépenses en quelques secondes
• Catégorisez automatiquement vos transactions
• Scannez vos reçus avec l'OCR intelligent
• Transactions récurrentes automatiques

💳 Gestion Multi-Comptes
• Gérez plusieurs comptes bancaires
• Visualisez votre solde global
• Transferts entre comptes

📈 Budgets Intelligents
• Créez des budgets mensuels par catégorie
• Alertes avant dépassement
• Suivi en temps réel de vos dépenses

🎯 Objectifs d'Épargne
• Définissez vos objectifs financiers
• Suivez votre progression
• Motivation quotidienne

📱 Widget Home Screen
• Consultez votre solde sans ouvrir l'app
• Mise à jour en temps réel

🌍 Multi-Langues
• Français, English, العربية, Español, Deutsch, हिन्दी, Português, Türkçe

🔒 SÉCURITÉ & CONFIDENTIALITÉ
• Toutes vos données restent sur votre appareil
• Aucune publicité
• Aucun tracking
• Backup chiffré optionnel (Google Drive)

⭐ VERSION PRO
• Comptes illimités
• Budgets illimités
• Objectifs d'épargne illimités
• Smart Insights (analyses avancées)
• Support prioritaire

💎 POURQUOI MONEYONE ?
• Interface moderne et intuitive
• 100% gratuit (version de base)
• Pas de publicité
• Données privées et sécurisées
• Support multilingue

📞 SUPPORT
Des questions ? Contactez-nous : support@moneyone.app

Téléchargez MoneyOne maintenant et commencez à économiser ! 💰
```

### Étape 3 : Assets graphiques
- Upload screenshots (minimum 2)
- Upload Feature Graphic (1024x500)
- Upload icône app (512x512) - déjà fait

### Étape 4 : Catégorisation
- **Catégorie** : Finance
- **Tags** : budget, finance, épargne, dépenses

### Étape 5 : Data Safety Form

**Données collectées** :
- ❌ Informations personnelles (nom, email)
- ❌ Informations financières (tout local)
- ❌ Localisation
- ✅ Photos/vidéos (CAMERA pour OCR)
- ✅ Identifiants appareil (Google Play Billing)

**Sécurité** :
- ✅ Chiffrement en transit (Firebase/Drive)
- ✅ Suppression possible (désinstallation)
- ❌ Partage avec tiers

**URL Privacy Policy** : `https://votre-url-github-pages.io/moneyone-privacy`

### Étape 6 : Content Rating (IARC)

Répondre au questionnaire :
- Violence : None
- Contenu sexuel : None
- Langage : None
- Achats in-app : **YES** (abonnement Pro)
- Accès internet : **YES**

**Rating attendu** : PEGI 3 / Everyone

### Étape 7 : Créer les produits In-App

**Produit 1 - Mensuel** :
- ID : `moneyone_pro_monthly`
- Type : Abonnement renouvelable
- Prix : 1,99 €/mois
- Période d'essai : 7 jours gratuits (recommandé)
- Description : "Accès illimité à toutes les fonctionnalités Pro"

**Produit 2 - Annuel** :
- ID : `moneyone_pro_annual`
- Type : Abonnement renouvelable
- Prix : 19,99 €/an
- Période d'essai : 7 jours gratuits
- Description : "Accès illimité Pro - Économisez 17%"

### Étape 8 : Upload AAB
1. Aller dans "Production" > "Créer une version"
2. Upload `app-release.aab`
3. Remplir les notes de version :

```
Version 1.1 - Première version publique

Nouveautés :
• Gestion complète de budget personnel
• Support multi-comptes et multi-devises
• Objectifs d'épargne avec suivi
• Smart Insights (Premium)
• Widget home screen
• Scan de reçus (OCR)
• 8 langues disponibles
```

### Étape 9 : Soumettre pour review
1. Vérifier tous les champs
2. Cliquer "Envoyer pour examen"
3. **Délai de review** : 24-48h

---

## ⏱️ TIMELINE DEMAIN

| Heure | Tâche | Durée |
|-------|-------|-------|
| 09:00 | Héberger Privacy Policy | 15 min |
| 09:15 | Créer screenshots | 30 min |
| 09:45 | Créer Feature Graphic | 20 min |
| 10:05 | Pause ☕ | 10 min |
| 10:15 | Remplir Play Console | 1h30 |
| 11:45 | Upload AAB + Submit | 15 min |
| **12:00** | **✅ SOUMIS !** | - |

---

## 🎯 APRÈS SOUMISSION

- **Review Google** : 24-48h
- **Publication** : Automatique après approbation
- **Monitoring** : Vérifier les crashs dans Play Console

---

## 📞 CONTACTS UTILES

- **Play Console** : https://play.google.com/console
- **Firebase Console** : https://console.firebase.google.com
- **Support Google Play** : https://support.google.com/googleplay/android-developer

---

## ✅ RÉSUMÉ TECHNIQUE

### Fonctionnalités implémentées
- ✅ Firebase intégré (Auth, Firestore)
- ✅ Google Play Billing (abonnements Pro)
- ✅ Bottom Navigation Bar (5 onglets)
- ✅ Écran Budgets complet
- ✅ Smart Insights Premium (5 cartes analytiques)
- ✅ Support multi-utilisateurs (userId)
- ✅ Permissions INTERNET + ACCESS_NETWORK_STATE
- ✅ ProGuard optimisé (obfuscation désactivée)
- ✅ ViewModel Factory implémentée
- ✅ Privacy Policy GDPR/CCPA compliant
- ✅ Symbole devise TND = "DT"
- ✅ Mode FREE testé (limitations fonctionnelles)

### Limitations Version FREE
- Maximum 2 comptes
- 1 budget global OU 3 budgets catégorie
- Maximum 1 objectif d'épargne
- Smart Insights bloqué

### Version PRO (1,99 €/mois ou 19,99 €/an)
- Comptes illimités
- Budgets illimités
- Objectifs illimités
- Smart Insights débloqué

---

**Tout est prêt ! Bonne chance pour demain ! 🚀**
