package com.example.photographapp.camera

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.photographapp.R
import com.example.photographapp.album.AlbumActivity
import com.example.photographapp.databinding.ActivityCameraBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraExcutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var camera: Camera
    private var isFlashOn = false

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        cameraExcutor = Executors.newSingleThreadExecutor()
        if (allPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
            startCamera()
        }
        binding.buttonFlash.setOnClickListener{
           switchFlash()
        }
        binding.buttonCapture.setOnClickListener {
            takePhoto()

        }
        binding.buttonSwitch.setOnClickListener {
            switchCamera()
        }
        binding.albumBtn.setOnClickListener {
            startActivity(Intent(this, AlbumActivity::class.java))
            Log.d("TAG", "click albumbtn")
        }

    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else CameraSelector.DEFAULT_BACK_CAMERA
        startCamera()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("takes photo", "onImageSaved: ")
                    Toast.makeText(this@CameraActivity, "Saved", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }
    private fun allPermissionGranted(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val storagePermission = if (android.os.Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        return cameraPermission && storagePermission
    }

    private fun startCamera() {
        binding.buttonFlash.visibility = View.GONE
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                Toast.makeText(this, "done initialize", Toast.LENGTH_SHORT).show()
                binding.buttonFlash.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }
    private fun switchFlash() {
        isFlashOn = !isFlashOn

        if (isFlashOn) {
            Log.d("TAG", "off: ")
            binding.buttonFlash.setImageResource(R.drawable.flash_on)
            camera.cameraControl.enableTorch(true)
        } else {
            Log.d("TAG", "on: ")
            binding.buttonFlash.setImageResource(R.drawable.flash_off)
            camera.cameraControl.enableTorch(false)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExcutor.shutdown()
    }
}
