package com.ethanprentice.networkchat.information_manager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.adt.UserInfo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class CacheManager(private val cacheDir: File) {

    private val userImgDir: File
    private val userDispNameFile: File

    init {
        userImgDir = File(cacheDir, USER_IMGS_PATH)
        userDispNameFile = File(cacheDir, USER_DISP_NAMES_FILENAME)

        if (!userImgDir.exists()) {
            userImgDir.mkdirs()
        }
        if (!userDispNameFile.exists()) {
            userDispNameFile.createNewFile()
        }
    }


    fun cacheUserInfo(ip: String, uInfo: UserInfo) {
        cacheUserImage(ip, uInfo.getImageBmp(MainApp.context))
        cacheUserDispName(ip, uInfo.displayName)
    }

    fun getUserInfo(ip: String): UserInfo? {
        val dispImg = getUserImage(ip)
        val dispName = getUserDispName(ip)

        return if (dispImg == null || dispName == null) {
            null
        }
        else {
            UserInfo(dispName, dispImg)
        }
    }

    fun getUserDispName(ip: String): String? {
        val userProperties = Properties()
        val inStream = FileInputStream(userDispNameFile)
        userProperties.load(inStream)
        inStream.close()

        val key = "${ip}_dispName"
        return userProperties.getProperty(key)
    }

    fun getUserImage(ip: String): Bitmap? {
        val file = File(userImgDir, ip)
        return if (!file.exists()) {
            null
        }
        else {
            BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    private fun cacheUserDispName(ip: String, dispName: String) {
        val userProperties = Properties()
        val inStream = FileInputStream(userDispNameFile)
        userProperties.load(inStream)
        inStream.close()

        val key = "${ip}_dispName"
        userProperties.setProperty(key, dispName)

        val outStream = FileOutputStream(userDispNameFile)
        userProperties.store(outStream, "--- No comment ---")
        outStream.close()
    }

    private fun cacheUserImage(ip: String, bmp: Bitmap) {
        val file = File(userImgDir, ip)
        val oStream = FileOutputStream(file)

        bmp.compress(Bitmap.CompressFormat.PNG, 100, oStream)
        oStream.flush()
        oStream.close()
    }


    companion object {
        private const val USER_IMGS_PATH = "profile_img"
        private const val USER_DISP_NAMES_FILENAME = "disp_names"
    }

}