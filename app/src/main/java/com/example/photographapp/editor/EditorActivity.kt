package com.example.photographapp.editor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Adapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.photographapp.ads.AdsHelper
import com.example.photographapp.album.AlbumActivity
import com.example.photographapp.data.UserRepository
import com.example.photographapp.databinding.ActivityEditorBinding
import com.example.photographapp.store.StoreActivity
import com.yalantis.ucrop.UCrop
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import java.io.File
import jp.co.cyberagent.android.gpuimage.filter.*

class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditorBinding
    private  lateinit var gpuImage: GPUImage
    private var currentBitmap: Bitmap? = null
    private  val brightnessFilter = GPUImageBrightnessFilter()
    private val contrastFilter = GPUImageContrastFilter()
    private val filterGroup = GPUImageFilterGroup()
    private var currentUri: Uri? =  null
    private lateinit var filterList: List<FilterModel>
    private val adsHelper = AdsHelper()
    lateinit var adapter: FilterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        adsHelper.loadRewardedAds(this)
        setContentView(binding.root)
        binding.recyclerFilters.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        if (UserRepository.PrefsManager.isPremium(this)){
            filterList = getFilters()
            filterList.forEach {
                it.isLocked = false
            }
        }else{
            filterList = getFilters()
        }

        val uri = intent.getStringExtra("image_uri")
        Glide.with(this).load(uri).into(binding.imageEditor)

        setGPUImage()
        loadImage()
        setupSeekBars()
        setupButtons()

        binding.btnCrop.setOnClickListener {
            startCrop()
        }
        initFilterList(filterList)

    }

    private fun initFilterList(filterList: List<FilterModel>) {
        adapter = FilterAdapter(
            this, filterList,
            currentBitmap!!){
            selectedFilter ->
            filterGroup.filters.clear()
            filterGroup.addFilter(brightnessFilter)
            filterGroup.addFilter(contrastFilter)
            filterGroup.addFilter(selectedFilter)
                val item = filterList.find { it.filter == selectedFilter }
                if (item?.isLocked == true) {
                    showUnlockDiaglog(selectedFilter)
                    return@FilterAdapter
                }

            gpuImage.setFilter(selectedFilter)
            updateImage()

        }

        binding.recyclerFilters.adapter = adapter
    }

    private fun showUnlockDiaglog(selectedFilter: GPUImageFilter) {
        val dialog = DialogUnlockFragment(
            onWatchAds = {showRewardedVideoAd(selectedFilter)},
            onBuyPremium = {
                goToStore()
            }
            )
        dialog.show(supportFragmentManager,"unlock_dialog")
    }

    private fun showRewardedVideoAd(selectedFilter: GPUImageFilter) {
        adsHelper.showRewardedAd(this,
            onRewardeEarned = {
                unlockfilter(selectedFilter)
            },
            onAdNotReady = {
                Toast.makeText(this, "Ad not ready", Toast.LENGTH_SHORT).show()

            }
        )

    }

    private fun unlockfilter(selectedFilter: GPUImageFilter) {
        var item = filterList.find { it.filter == selectedFilter }
        item!!.isLocked = false
        adapter.notifyItemChanged(filterList.indexOf(item))
        gpuImage.setFilter(selectedFilter)
        updateImage()
    }

    private fun goToStore() {
        startActivity(Intent(this, StoreActivity::class.java))
    }

    private fun getFilters(): List<FilterModel> {
        return listOf(
            FilterModel("Normal", GPUImageFilter()),

            FilterModel("Brightness", GPUImageBrightnessFilter()),
            FilterModel("Contrast", GPUImageContrastFilter()),
            FilterModel("Exposure", GPUImageExposureFilter()),
            FilterModel("Saturation", GPUImageSaturationFilter()),

            FilterModel("Grayscale", GPUImageGrayscaleFilter(), isLocked = true),

            FilterModel("Invert", GPUImageColorInvertFilter(), isLocked = true),

            FilterModel("Blur", GPUImageGaussianBlurFilter(), isLocked = true),
            FilterModel("Sharpen", GPUImageSharpenFilter(), isLocked = true),

            FilterModel("Sketch", GPUImageSketchFilter(), isLocked = true),
            FilterModel("Toon", GPUImageToonFilter(), isLocked = true)
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP){
            when(resultCode){
                RESULT_OK -> {
                    val resultUri = UCrop.getOutput(data!!)
                }

                RESULT_CANCELED -> {
                    Toast.makeText(this, "Crop canceled", Toast.LENGTH_SHORT).show()
                }
                UCrop.RESULT_ERROR -> {
                    val error = UCrop.getError(data!!)
                    Log.e("crop error", error.toString() )
                }
            }

        }
    }

    private fun startCrop() {
        val bitmap = currentBitmap ?: return
        val sourceUri = getImageUrifromBitmap(bitmap)
        val destinationUri = Uri.fromFile(
            File(cacheDir, "croped_${System.currentTimeMillis()}.jpg")
        )
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)
            setHideBottomControls(true)
            setToolbarTitle("Crop Image")
        }
        UCrop.of(sourceUri, destinationUri).withOptions(options).start(this)
    }

    private fun getImageUrifromBitmap(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        val fos = file.outputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
        return Uri.fromFile(file)
    }

    // Load ảnh từ Album (URI)
    private fun loadImage() {
        val uriString = intent.getStringExtra("image_uri") ?: return
        val uri = Uri.parse(uriString)
        currentUri = uri
        val source = ImageDecoder.createSource(contentResolver, uri)

        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.setMutableRequired(true)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE 
        }

        // Convert về ARGB_8888 (fix crash GPUImage)
        val safeBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        currentBitmap = safeBitmap
        gpuImage.setImage(safeBitmap)
        updateImage()
    }

    // setup GPUImage filter group
    private fun setGPUImage() {
        gpuImage = GPUImage(this)

        filterGroup.addFilter(brightnessFilter)
        filterGroup.addFilter(contrastFilter)

        gpuImage.setFilter(filterGroup)
    }

    // SeekBar Brightness + Contrast
    private fun setupSeekBars() {

        // Brightness (-1 → 1)
        binding.seekBrightness.max = 200
        binding.seekBrightness.progress = 100

        binding.seekBrightness.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = (progress - 100) / 100f
                    brightnessFilter.setBrightness(value)
                    updateImage()
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })

        // Contrast (0 → 2)
        binding.seekContrast.max = 200
        binding.seekContrast.progress = 100

        binding.seekContrast.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = progress / 100f
                    contrastFilter.setContrast(value)
                    updateImage()
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
    }

    // Buttons: Rotate + Save
    private fun setupButtons() {

        // Rotate 90 độ
        binding.btnRotate.setOnClickListener {
            currentBitmap?.let {
                val matrix = Matrix()
                matrix.postRotate(90f)

                val rotated = Bitmap.createBitmap(
                    it, 0, 0, it.width, it.height, matrix, true
                )

                currentBitmap = rotated
                gpuImage.setImage(rotated)
                updateImage()
            }
        }

        // Save ảnh
        binding.btnSave.setOnClickListener {
            saveImage()
        }
    }

    // Apply filter và hiển thị
    private fun updateImage() {
        val result = gpuImage.bitmapWithFilterApplied
        binding.imageEditor.setImageBitmap(result)
        Log.d("updateImage", "updateImage: $result")
    }

    // Lưu ảnh vào gallery
    private fun saveImage() {
        val bitmap = gpuImage.bitmapWithFilterApplied ?: return

        val filename = "EDIT_${System.currentTimeMillis()}.jpg"

        MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            filename,
            "Edited Image"
        )

        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, AlbumActivity::class.java))
    }
}