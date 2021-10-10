package com.example.criminalintent

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.roundToInt

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true     //using this field we can avoid memory allocation while getting image's size
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    var inSampleSize = 1            //this property shows how many pixels of raw image final image will contain
    if (srcWidth > destWidth || srcHeight > destHeight) {   //here we are scaling image to destination size
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        val sampleScale = if (heightScale > widthScale)
            heightScale
        else {
            widthScale
        }
        inSampleSize = sampleScale.roundToInt()
    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize


    return BitmapFactory.decodeFile(path, options)
}
