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

class BillingHelper(
    private val context: Context,
    private val onPurchaseSuccess: () -> Unit,
    private val onBillingReady: () -> Unit // 👈 thêm callback
) {
    private lateinit var userState: UserState
    lateinit var billingClient: BillingClient
    private val PRODUCT_ID = "basic.test"

    fun startConnection() {
        Log.d("TAG", "startConnection")

        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                Log.d("TAG", "Purchase listener: ${billingResult.responseCode}")

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
                Log.d("TAG", "Disconnected → retry")
                startConnection()
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                Log.d("TAG", "Billing setup: ${result.responseCode}")

                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("TAG", "Billing READY")

                    onBillingReady()   // 🔥 chỉ lúc này mới load product
                    queryPurchase()    // restore purchase
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
            .setProductDetailsParamsList(
                listOf(productParamsBuilder.build())
            )
            .build()

        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Toast.makeText(activity, "Transaction is failed", Toast.LENGTH_SHORT).show()
        }

    }

    fun queryPurchase() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { _, purchases ->
            Log.d("TAG", "Restore purchases: ${purchases.size}")

            purchases.forEach {
                handlePurchase(it)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        Log.d("TAG", "handlePurchase")

        if (purchase.products.contains(PRODUCT_ID) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        ) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                // Unlock feature + set prefs
                userState = UserState(true)
                UserRepository.PrefsManager.setPremium(context, userState.isPremium)
                onPurchaseSuccess()
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        Log.d("TAG", "acknowledgePurchase")

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                onPurchaseSuccess()
            }
        }
    }
}