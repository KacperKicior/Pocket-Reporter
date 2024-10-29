package com.example.rss_news

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView
import java.net.URL

class ImageLoaderTask(private val imageView: ImageView?) : AsyncTask<String, Void, Bitmap?>() {

    override fun doInBackground(vararg params: String?): Bitmap? {
        val urlString = params[0]
        return try {
            val url = URL(urlString)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        result?.let {
            imageView?.setImageBitmap(it)
        }
    }
}
