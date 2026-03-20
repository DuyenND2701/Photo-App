package com.example.photographapp.album

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photographapp.databinding.ActivityAlbumBinding
import com.example.photographapp.editor.EditorActivity

class AlbumActivity : AppCompatActivity() {
    private val photoList = mutableListOf<Photo>()
    private lateinit var binding: ActivityAlbumBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TAG", "start album activity")
        binding = ActivityAlbumBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.recyclerAlbum.layoutManager = GridLayoutManager(this, 2)
        loadPhotos()


    }

    private fun loadPhotos() {

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
           MediaStore.Images.Media._ID)
        val cursor = contentResolver.query(collection, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC")
        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            if (cursor == null || !cursor.moveToFirst()) {
                binding.none.visibility = View.VISIBLE
                binding.recyclerAlbum.visibility = View.GONE
                return
            }
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id)
                photoList.add(Photo(uri.toString()))
            }
        }
        val adapter = AlbumAdapter(photoList){
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra("image_uri",it.uri)
            startActivity(intent)
            Log.d("TAG", "loadPhotos: change Activity")
        }
        binding.recyclerAlbum.adapter = adapter
        Log.d("TAG", "call load Phôtos ")
    }

}