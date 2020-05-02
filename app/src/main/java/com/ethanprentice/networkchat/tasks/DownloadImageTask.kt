package com.ethanprentice.networkchat.tasks

import android.os.AsyncTask
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log


class DownloadImageTask(private val url: String) : AsyncTask<Void, Void, Bitmap>() {

    override fun doInBackground(vararg args: Void): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val inStream = java.net.URL(url).openStream()
            bitmap = BitmapFactory.decodeStream(inStream)
        } catch (e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
        }

        return bitmap
    }
}