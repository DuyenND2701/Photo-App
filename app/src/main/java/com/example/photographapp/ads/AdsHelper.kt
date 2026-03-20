package com.example.photographapp.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.photographapp.editor.EditorActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdsHelper() {
    private var rewardedAd: RewardedAd? = null

    fun loadRewardedAds(context: Context){
        RewardedAd.load(
            context,
            "ca-app-pub-3940256099942544/5224354917",
            AdManagerAdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("load RewardAds", "Ad was loaded ")
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(ad: LoadAdError) {
                    Log.d("load RewardAds", "onAdFailedToLoad: ${ad.message}")
                    rewardedAd = null
                }
            }
        )
    }
    fun showRewardedAd(activity: Activity,
                       onRewardeEarned: () -> Unit,
                       onAdNotReady: () -> Unit){
        if (rewardedAd != null){
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback(){
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAds(activity)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    rewardedAd = null
                    Toast.makeText(activity, "ad failed to show fullscreen", Toast.LENGTH_SHORT)
                        .show()

                }
            }
            rewardedAd?.show(activity){
                onRewardeEarned()
            }
        } else {
            onAdNotReady()
            loadRewardedAds(activity)
        }
    }
}