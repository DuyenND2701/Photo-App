package com.example.photographapp.initialize

import android.app.Application
import android.util.Log
import com.example.photographapp.billing.BillingHelper

class AppConfig : Application() {

    lateinit var billingHelper: BillingHelper

    override fun onCreate() {
        super.onCreate()

        billingHelper = BillingHelper(this)

        billingHelper.onBillingReady = {
            Log.d("TAG", "Billing ready (App)")
        }

        billingHelper.onPurchaseSuccess = {
            Log.d("TAG", "Purchase success (App)")
        }

        billingHelper.onRestoreResult = { hasPurchase ->
            Log.d("TAG", "Restore result (App): $hasPurchase")
        }

        billingHelper.startConnection()
    }
}