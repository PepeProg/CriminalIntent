package com.example.criminalintent

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

private const val EXTRA_IMAGE = "extraImage"
private const val ROTATION = "Rotation"

class PhotoZoomDialog: DialogFragment() {
    private lateinit var zoomImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_zoom_photo, container, false)
        zoomImage = view.findViewById(R.id.zoom_image) as ImageView
        return view
    }

    override fun onStart() {
        super.onStart()
        val bitmap = arguments?.getParcelable(EXTRA_IMAGE) as Bitmap?
        val rotation = arguments?.getFloat(ROTATION) as Float
        bitmap?.let {
            zoomImage.setImageBitmap(it)
        }
        zoomImage.rotation = rotation
    }

    companion object {
        fun getInstance(bitmap: Bitmap, rotation: Float): PhotoZoomDialog {
            val zoomDialog = PhotoZoomDialog()
            val args = Bundle().apply {
                putParcelable(EXTRA_IMAGE, bitmap)
                putFloat(ROTATION, rotation)
            }
            return zoomDialog.apply {
                arguments = args
            }
        }
    }
}