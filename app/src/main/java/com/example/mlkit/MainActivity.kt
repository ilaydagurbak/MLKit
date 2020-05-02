package com.example.mlkit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mlkit.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isText = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        switch1.setOnClickListener {
            isText = switch1.isChecked
        }
        binding.fab.setOnClickListener {
            pickImage()
        }
    }

    private fun pickImage() {
        val intent = Intent().apply {
            action = Intent.ACTION_PICK
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    val bitmap = getImageFromData(data)
                    bitmap?.apply {
                        imageView.setImageBitmap(this)

                        if (!isText) {
                            processImageTagging(bitmap)
                        }
                        if (isText) {
                            startTextRecognizing(bitmap)
                        }

                    }
                }
            }
        }
        super.onActivityResult(
            requestCode, resultCode,
            data
        )
    }

    private fun processImageTagging(bitmap: Bitmap) {
        val visionImg = FirebaseVisionImage.fromBitmap(bitmap)
        FirebaseVision.getInstance().onDeviceImageLabeler.processImage(visionImg)
            .addOnSuccessListener { tags ->
                binding.chipGroup.removeAllViews()
                tags.sortedByDescending { it.confidence }
                    .map {
                        Chip(this, null, R.style.Widget_MaterialComponents_Chip_Choice)
                            .apply { text = it.text }
                    }.forEach {
                        binding.chipGroup.addView(it)
                    }
            }
            .addOnFailureListener { ex ->
                Log.wtf("LAB", ex)
            }
    }

    private fun getImageFromData(data: Intent?): Bitmap? {
        val selectedImage = data?.data
        return MediaStore.Images.Media.getBitmap(
            this.contentResolver,
            selectedImage
        )
    }

    private fun startTextRecognizing(bitmap: Bitmap) {
        if (binding.imageView.drawable != null) {
            //Initialize input object
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            //Initialize the on-device detector
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    // Task completed successfully
                    processTextBlock(firebaseVisionText)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    binding.textView.text = "Fail!"
                }
        } else {
            Toast.makeText(this, "Fail!", Toast.LENGTH_LONG).show()
        }
    }

    private fun processTextBlock(result: FirebaseVisionText) {
        binding.chipGroup.removeAllViews()
        result.textBlocks.map {
            Chip(this, null, R.style.Widget_MaterialComponents_Chip_Choice)
                .apply { text = it.text }
        }.forEach {
            binding.chipGroup.addView(it)
        }
    }



    companion object {
        private var IMAGE_PICK_CODE = 180
    }

}
