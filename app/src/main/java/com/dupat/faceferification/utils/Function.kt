package com.dupat.faceferification.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream


object Function {

    fun bitmapToByteArray(bmp: Bitmap): ByteArray{
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        bmp.recycle()
        return byteArray
    }

    fun byteArrayToBitmap(arr: ByteArray): Bitmap{
        val bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.size)
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }
}