package com.example.photographapp.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.photographapp.data.UserRepository
import com.example.photographapp.data.UserState
import com.example.photographapp.store.StoreActivity

class BillingHelper(
    private val context: Context
) {

    lateinit var billingClient: BillingClient
    private val PRODUCT_ID = "basic.test"

    // callback
    var onPurchaseSuccess: (() -> Unit)? = null
    var onBillingReady: (() -> Unit)? = null
    var onRestoreResult: ((Boolean) -> Unit)? = null

    fun startConnection() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases?.forEach {
                        handlePurchase(it)
                    }
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                startConnection()
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    onBillingReady?.invoke()
                    queryPurchase()
                }
            }
        })
    }

    fun launchPurchase(activity: Activity, productDetails: ProductDetails) {

        val productParamsBuilder =
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)

        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerToken = productDetails.subscriptionOfferDetails
                ?.firstOrNull()
                ?.offerToken

            offerToken?.let {
                productParamsBuilder.setOfferToken(it)
            }
        }

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        billingClient.launchBillingFlow(activity, params)
    }

    fun queryPurchase() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { _, purchases ->

            if (purchases.isEmpty()) {
                UserRepository.PrefsManager.setPremium(context, false)
                onRestoreResult?.invoke(false)
                return@queryPurchasesAsync
            }

            purchases.forEach {
                handlePurchase(it)
            }

            onRestoreResult?.invoke(true)
        }
    }

    private fun handlePurchase(purchase: Purchase) {

        if (purchase.products.contains(PRODUCT_ID) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        ) {

            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                savePremium()
                onPurchaseSuccess?.invoke()
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                savePremium()
                onPurchaseSuccess?.invoke()
            }
        }
    }

    private fun savePremium() {
        UserRepository.PrefsManager.setPremium(context, true)
    }
}