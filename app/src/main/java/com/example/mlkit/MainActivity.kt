package com.example.mlkit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            pickImage()
        }

    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, Companion.PHOTO_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Companion.PHOTO_REQUEST_CODE -> {
                    val bitmap = getImageFromData(data)
                    bitmap?.apply {
                        photoIV.setImageBitmap(this)
                        processImageTagging(bitmap)
                    }
                }
            }
        }
        super.onActivityResult(
            requestCode, resultCode,
            data
        )
    }

    private fun getImageFromData(data: Intent?): Bitmap? {
        val selectedImage = data?.data
        return MediaStore.Images.Media.getBitmap(
            this.contentResolver,
            selectedImage
        )
    }

    private fun processImageTagging(bitmap: Bitmap) {
        val visionImg = FirebaseVisionImage.fromBitmap(bitmap)
        FirebaseVision.getInstance().onDeviceImageLabeler.processImage(visionImg)
            .addOnSuccessListener { tags ->
                labelsTV.text = tags.sortedByDescending { it.confidence }
                    .joinToString(" ") { it.text }
            }
            .addOnFailureListener { ex ->
                Log.wtf("LAB", ex)
            }
    }

    companion object {
        private const val PHOTO_REQUEST_CODE = 180
    }

}
