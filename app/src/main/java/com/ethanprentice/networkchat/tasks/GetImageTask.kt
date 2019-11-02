package com.ethanprentice.networkchat.tasks

import android.os.AsyncTask

class GetImageTask(_imgUrl: String, _timeout: Int = 200) : AsyncTask<Void, Void, Unit>() {

    val imgUrl = _imgUrl
    val timeout = _timeout

    override fun doInBackground(vararg p0: Void?) {


    }


}