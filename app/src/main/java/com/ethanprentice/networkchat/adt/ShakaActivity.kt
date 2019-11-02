package com.ethanprentice.networkchat.adt

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.ethanprentice.networkchat.MainApp


abstract class ShakaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainApp.currActivity = this
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        MainApp.currActivity = this
    }

    override fun onResume() {
        super.onResume()
        MainApp.currActivity = this
    }

    override fun onPause() {
        clearCurrActivity()
        super.onPause()
    }

    override fun onDestroy() {
        clearCurrActivity()
        super.onDestroy()
    }

    private fun clearCurrActivity() {
        if (MainApp.currActivity == this) {
            MainApp.currActivity = null
        }
    }

}