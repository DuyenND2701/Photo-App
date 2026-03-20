package com.example.photographapp.album

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.photographapp.R
import com.example.photographapp.databinding.PhotoItemViewholderBinding

class AlbumAdapter(
    private val photos: List<Photo>,
    private val onClick: (Photo) -> Unit):
    RecyclerView.Adapter<AlbumAdapter.PhotoViewHolder>() {
    class PhotoViewHolder(val binding: PhotoItemViewholderBinding): RecyclerView.ViewHolder(binding.root)

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val binding = PhotoItemViewholderBinding.inflate(inflater, parent, false)
        return PhotoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        Log.d("TAG", "call load adapter album ")
        val photo = photos[position]
        Glide.with(holder.itemView.context).load(photo.uri).into(holder.binding.imagePhoto)
        holder.binding.root.setOnClickListener{
            onClick(photo)
        }

    }

}