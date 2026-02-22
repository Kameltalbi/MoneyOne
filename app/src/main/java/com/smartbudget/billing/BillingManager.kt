package com.smartbudget.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_ID_MONTHLY = "moneyone_pro_monthly"
        const val PRODUCT_ID_ANNUAL = "moneyone_pro_annual"
        private const val PREFS_NAME = "moneyone_pro"
        private const val KEY_IS_PRO = "is_pro"
        
        // DEV MODE: Set to true for personal testing APK (bypasses Google Play Billing)
        // IMPORTANT: Must be false for Play Store release!
        private const val DEV_MODE_FORCE_PRO = true
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Initialize Pro status: DEV_MODE forces Pro, otherwise checks subscription
    private val _isPro = MutableStateFlow(
        if (DEV_MODE_FORCE_PRO) true 
        else prefs.getBoolean(KEY_IS_PRO, false)
    )
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private var billingClient: BillingClient? = null
    private var productDetails: List<ProductDetails> = emptyList()

    private val _monthlyPrice = MutableStateFlow("1,99 €")
    val monthlyPrice: StateFlow<String> = _monthlyPrice.asStateFlow()

    private val _annualPrice = MutableStateFlow("19,99 €")
    val annualPrice: StateFlow<String> = _annualPrice.asStateFlow()

    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_ANNUAL)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, details ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = details
                details.forEach { product ->
                    val offer = product.subscriptionOfferDetails?.firstOrNull()
                    val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: ""
                    when (product.productId) {
                        PRODUCT_ID_MONTHLY -> _monthlyPrice.value = price
                        PRODUCT_ID_ANNUAL -> _annualPrice.value = price
                    }
                }
            }
        }
    }

    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            (purchase.products.contains(PRODUCT_ID_MONTHLY) ||
                                    purchase.products.contains(PRODUCT_ID_ANNUAL))
                }
                setProStatus(hasActiveSub)

                // Acknowledge unacknowledged purchases
                purchases.filter { !it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .forEach { purchase ->
                        val ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient?.acknowledgePurchase(ackParams) { }
                    }
            }
        }
    }

    fun launchSubscription(activity: Activity, isAnnual: Boolean) {
        val productId = if (isAnnual) PRODUCT_ID_ANNUAL else PRODUCT_ID_MONTHLY
        val product = productDetails.firstOrNull { it.productId == productId } ?: return
        val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    setProStatus(true)
                    if (!purchase.isAcknowledged) {
                        val ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient?.acknowledgePurchase(ackParams) { }
                    }
                }
            }
        }
    }

    private fun setProStatus(isPro: Boolean) {
        // In DEV_MODE, always keep Pro enabled
        if (DEV_MODE_FORCE_PRO) {
            prefs.edit().putBoolean(KEY_IS_PRO, true).apply()
            _isPro.value = true
        } else {
            prefs.edit().putBoolean(KEY_IS_PRO, isPro).apply()
            _isPro.value = isPro
        }
    }

    fun destroy() {
        billingClient?.endConnection()
    }
}
