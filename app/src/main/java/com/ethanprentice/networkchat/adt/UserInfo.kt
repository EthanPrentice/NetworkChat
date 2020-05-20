package com.ethanprentice.networkchat.adt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.adt.serialization.BitmapSerializer
import com.ethanprentice.networkchat.util.ImageUtils
import kotlinx.serialization.Serializable
import java.lang.IllegalStateException

/**
 * User specific information like Spotify display userDispName, profile picture urls, etc.
 *
 * @author Ethan Prentice
 */
@Serializable
class UserInfo(
        var displayName: String,

        @Serializable(with=BitmapSerializer::class)
        private var imageBmp: Bitmap?
) {

    fun getImageBmp(context: Context): Bitmap {
        return imageBmp ?: getDefaultImage(context)
    }

    fun changeBmpResolution(xPixels: Int, yPixels: Int) {
            imageBmp?.let {
                    imageBmp = Bitmap.createScaledBitmap(it, xPixels, yPixels, false)
            }
    }

    companion object {
        private var defaultBitmap: Bitmap? = null

        fun getDefaultImage(context: Context): Bitmap {
            if (defaultBitmap == null) {
                val drawable = context.resources.getDrawable(R.drawable.ic_account_circle_dark, null)
                defaultBitmap = when (drawable) {
                    is VectorDrawable -> ImageUtils.getBitmap(drawable)
                    is BitmapDrawable -> drawable.bitmap
                    else -> throw IllegalStateException("Default drawable should be a VectorDrawable or BitmapDrawable!")
                }
            }
            return defaultBitmap!!
        }
    }
}