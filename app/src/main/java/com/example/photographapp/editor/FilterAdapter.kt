package com.example.photographapp.editor

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.photographapp.R
import com.example.photographapp.databinding.FilterItemViewholderBinding
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter

class FilterAdapter(
    private val context: Context,
    private val filters: List<FilterModel>,
    private val originalImage: Bitmap,
    private val setFiltertoPreview: (GPUImageFilter) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {
    class FilterViewHolder(val binding: FilterItemViewholderBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val binding = FilterItemViewholderBinding.inflate(
            inflater, parent, false
        )
        return FilterViewHolder(binding)
    }

    override fun getItemCount() = filters.size

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
       val item = filters[position]
        if (item.preview == null){
            val thumb = Bitmap.createScaledBitmap(originalImage, 200, 200, false)
            val gpuImage = GPUImage(context)
            gpuImage.setImage(thumb)
            gpuImage.setFilter(item.filter)
            item.preview = gpuImage.bitmapWithFilterApplied
        }
        holder.binding.imageFilter.setImageBitmap(item.preview)
        holder.binding.txtName.text = item.name
        if (item.isLocked){
            holder.binding.imageLock.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.imageLock.visibility = android.view.View.GONE
        }
        holder.binding.filterItem.setOnClickListener{
            setFiltertoPreview.invoke(item.filter)

        }
    }


}