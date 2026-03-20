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
import com.example.photographapp.initialize.AppConfig

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

        // 🔥 Lấy instance global từ MyApp
        billingHelper = (application as AppConfig).billingHelper

        billingHelper.onPurchaseSuccess = {
            runOnUiThread {
                Toast.makeText(this, "Transaction success", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AlbumActivity::class.java))
                finish() // 🔥 thêm dòng này
            }
        }

        billingHelper.onRestoreResult = { hasPurchase ->
            runOnUiThread {
                if (hasPurchase) {
                    Toast.makeText(this, "Restore success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AlbumActivity::class.java))

                } else {
                    Toast.makeText(this, "You haven't buy purchase yet!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        loadPurchasePackage()
    }

    private fun loadPurchasePackage() {
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
            if (productList.isNotEmpty()) {
                Log.d("TAG", "loadPurchasePackage: ${productList.size}")
                Log.d("TAG", "loadPurchasePackage: ${productList}")
                productDetails = productList[0]

                runOnUiThread {
                    binding.txtPrice.text =
                        productDetails!!
                            .subscriptionOfferDetails
                            ?.first()
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.first()
                            ?.formattedPrice ?: "53 000"
                }
            }
        }
    }

    private fun setupClick() {

        binding.btnBuy.setOnClickListener {
            productDetails?.let {
                billingHelper.launchPurchase(this, it)
            } ?: run {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRestore.setOnClickListener {
            billingHelper.queryPurchase()
            Toast.makeText(this, "Restoring...", Toast.LENGTH_SHORT).show()
        }
    }
}