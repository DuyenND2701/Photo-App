package com.example.photographapp.editor

import android.graphics.Bitmap
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter

data class FilterModel(
    val name: String,
    val filter: GPUImageFilter,
    var preview: Bitmap? = null,
    var isLocked: Boolean = false
)
