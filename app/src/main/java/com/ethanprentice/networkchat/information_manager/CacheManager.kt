package com.ethanprentice.networkchat.information_manager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class CacheManager(private val cacheDir: File) {

    private val profileImgDir: File

    init {
        profileImgDir = File(cacheDir, PROFILE_IMG_PATH)

        if (!profileImgDir.exists()) {
            profileImgDir.mkdirs()
        }
    }


    fun getProfileImageByIp(ip: String): Bitmap? {
        val file = File(profileImgDir, ip)
        return if (file.exists()) {
            null
        }
        else {
            BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    fun setProfileImage(ip: String, bmp: Bitmap) {
        val file = File(profileImgDir, ip)
        val oStream = FileOutputStream(file)

        bmp.compress(Bitmap.CompressFormat.PNG, 100, oStream)
        oStream.flush()
        oStream.close()
    }


    companion object {
        private const val PROFILE_IMG_PATH = "profile_img"
    }

}