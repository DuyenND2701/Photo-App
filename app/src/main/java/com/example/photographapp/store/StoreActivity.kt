package com.example.photographapp.store

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.example.photographapp.R
import com.example.photographapp.album.AlbumActivity
import com.example.photographapp.billing.BillingHelper
import com.example.photographapp.data.UserRepository
import com.example.photographapp.databinding.ActivityAlbumBinding
import com.example.photographapp.databinding.ActivityStoreBinding

class StoreActivity : AppCompatActivity() {

    private lateinit var billingHelper: BillingHelper
    private lateinit var binding: ActivityStoreBinding
    private var productDetails: ProductDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBilling()
        setupClick()
    }

    private fun setupBilling() {
        Log.d("TAG", "setupBilling")

        billingHelper = BillingHelper(
            context = this,
            onPurchaseSuccess = {
                startActivity(Intent(this, AlbumActivity::class.java))
                Toast.makeText(this, "Transaction is success", Toast.LENGTH_SHORT).show()},
            onBillingReady = {
                loadPurchasePackage() // 🔥 chỉ gọi khi billing READY
            }
        )

        billingHelper.startConnection()
    }

    private fun loadPurchasePackage() {
        Log.d("TAG", "loadPurchasePackage")

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("basic.test")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingHelper.billingClient.queryProductDetailsAsync(params) { _, productList ->
            Log.d("TAG", "Product size: ${productList.size}")
            Log.d("TAG", "Package: ${applicationContext.packageName}")
            if (productList.isNotEmpty()) {
                productDetails = productList[0]
                Log.d("TAG", "loadPurchasePackage: ${productDetails}")
                runOnUiThread {
                    binding.txtPrice.text =
                        (productDetails!!.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.first()?.formattedPrice) ?: "53 0000 VND"

                }
            }
        }
    }

    private fun setupClick() {
        binding.btnBuy.setOnClickListener {
            Log.d("TAG", "setupClick: ${productDetails}")
            productDetails?.let {
                billingHelper.launchPurchase(this, it)

            } ?: run {
                Toast.makeText(this, "Product not found", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnRestore.setOnClickListener {
            billingHelper.queryPurchase()
            Toast.makeText(this, "Restoring purchases...", Toast.LENGTH_SHORT).show()
        }
    }
}