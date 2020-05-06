package com.ethanprentice.networkchat.adt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.adt.serialization.BitmapSerializer
import kotlinx.serialization.Serializable

/**
 * User specific information like Spotify display name, profile picture urls, etc.
 *
 * @author Ethan Prentice
 */
@Serializable
class UserInfo(
        var displayName: String,

        @Serializable(with=BitmapSerializer::class)
        var imageBmp: Bitmap?
)  {

        fun changeBmpResolution(xPixels: Int, yPixels: Int) {
                imageBmp?.let {
                        imageBmp = Bitmap.createScaledBitmap(it, xPixels, yPixels, false)
                }
        }

        companion object {
                fun getDefaultImage(context: Context): Bitmap {
                        return BitmapFactory.decodeResource(context.resources, R.drawable.ic_account_circle_dark)
                }
        }
}