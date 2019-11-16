package com.ethanprentice.networkchat.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.information_manager.InfoManager


class ChatMessageView(context: Context, val ip: String, message: String, private val sender: UserInfo): RelativeLayout(context) {

    private val msgTextView = TextView(context)
    private val uDispImgView = ImageView(context)
    private val innerLayout = RelativeLayout(context)


    init {
        setLayoutParams()
        initInnerLayout()

        msgTextView.text = message
        msgTextView.textSize = 20f
        msgTextView.setTextColor(resources.getColor(R.color.black))
        msgTextView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        (msgTextView.layoutParams as LayoutParams).addRule(CENTER_VERTICAL)
        msgTextView.setPadding(20, 10, 20, 10)

        innerLayout.addView(msgTextView)
        addView(innerLayout)
    }


    private fun initInnerLayout() {
        innerLayout.setPadding(15, 15, 15, 15)
        innerLayout.layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        )

        innerLayout.minimumHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt()
        innerLayout.minimumWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics).toInt()

        innerLayout.setBackgroundResource(R.drawable.rounded_rect_bg)

        if (ip == InfoManager.getDeviceIp().canonicalHostName) {
            (innerLayout.layoutParams as LayoutParams).addRule(ALIGN_PARENT_RIGHT)
            innerLayout.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.lightBlue))
        }
        else {
            (innerLayout.layoutParams as LayoutParams).addRule(ALIGN_PARENT_LEFT)
            innerLayout.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.offWhite))
        }
    }

    fun setLayoutParams() {
        val params = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        )
        params.setMargins(15, 15, 15, 15)
        layoutParams = params
    }
}