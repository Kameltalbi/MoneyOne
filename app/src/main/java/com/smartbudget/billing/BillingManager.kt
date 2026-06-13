package com.smartbudget.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.smartbudget.config.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        const val SUBSCRIPTION_ID = "moneyone_premium"
        const val BASE_PLAN_MONTHLY = "monthly"
        const val BASE_PLAN_ANNUAL = "yearly"
        private const val PREFS_NAME = "moneyone_pro"
        private const val KEY_IS_PRO = "is_pro"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isPro = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private var billingClient: BillingClient? = null
    private var productDetails: List<ProductDetails> = emptyList()

    private val _monthlyPrice = MutableStateFlow("2,99 $")
    val monthlyPrice: StateFlow<String> = _monthlyPrice.asStateFlow()

    private val _annualPrice = MutableStateFlow("29,99 $")
    val annualPrice: StateFlow<String> = _annualPrice.asStateFlow()

    private val _lifetimePrice = MutableStateFlow("49,99 $")
    val lifetimePrice: StateFlow<String> = _lifetimePrice.asStateFlow()

    fun initialize() {
        // TEST MODE: Force PRO access for closed testing
        if (AppConfig.IS_TEST_MODE) {
            android.util.Log.d("BILLING_DEBUG", "TEST MODE: Forcing PRO access")
            _isPro.value = true
            return
        }
        
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
                .setProductId(SUBSCRIPTION_ID)
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
                    if (product.productId == SUBSCRIPTION_ID) {
                        product.subscriptionOfferDetails?.forEach { offer ->
                            val price = offer.pricingPhases.pricingPhaseList.firstOrNull()?.formattedPrice ?: ""
                            when (offer.basePlanId) {
                                BASE_PLAN_MONTHLY -> _monthlyPrice.value = price
                                BASE_PLAN_ANNUAL -> _annualPrice.value = price
                            }
                        }
                    }
                }
            }
        }
    }

    private fun queryPurchases() {
        android.util.Log.d("BILLING_DEBUG", "queryPurchases() called, IS_TEST_MODE = ${AppConfig.IS_TEST_MODE}")
        
        if (AppConfig.IS_TEST_MODE) {
            android.util.Log.d("BILLING_DEBUG", "TEST MODE: queryPurchases skipped")
            return
        }
        
        android.util.Log.d("BILLING_DEBUG", "PROD MODE: Querying purchases...")
        
        // Check subscriptions
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                            purchase.products.contains(SUBSCRIPTION_ID)
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
        val basePlanId = if (isAnnual) BASE_PLAN_ANNUAL else BASE_PLAN_MONTHLY
        val product = productDetails.firstOrNull { it.productId == SUBSCRIPTION_ID } ?: return
        val offer = product.subscriptionOfferDetails?.firstOrNull { it.basePlanId == basePlanId } ?: return

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
            .setOfferToken(offer.offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    // Lifetime purchase removed - only monthly and annual subscriptions available

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
        android.util.Log.d("BILLING_DEBUG", "setProStatus($isPro) called, IS_TEST_MODE = ${AppConfig.IS_TEST_MODE}")
        
        if (AppConfig.IS_TEST_MODE) {
            android.util.Log.d("BILLING_DEBUG", "TEST MODE: setProStatus ignored (PRO forced)")
            return
        }
        
        android.util.Log.d("BILLING_DEBUG", "PROD MODE: Setting isPro to $isPro")
        _isPro.value = isPro
        prefs.edit().putBoolean(KEY_IS_PRO, isPro).apply()
    }

    fun destroy() {
        billingClient?.endConnection()
    }
}
