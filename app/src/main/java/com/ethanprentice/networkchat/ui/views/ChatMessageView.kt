package com.ethanprentice.networkchat.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.information_manager.InfoManager
import kotlin.math.ceil


class ChatMessageView(context: Context, val ip: String, message: String, private val sender: UserInfo): LinearLayout(context) {

    // holds text message and user display image
    private val infoContainer = RelativeLayout(context)

    private val msgTextView = TextView(ContextThemeWrapper(context, R.style.ChatMessageTextStyle), null, 0)
    private val uDispImgView = ImageView(context)

    private val spacerView = View(context)

    private val sentByThisDevice = (ip == InfoManager.deviceIp.canonicalHostName)

    init {
        configureLayoutParams()
        orientation = LinearLayout.HORIZONTAL

        msgTextView.text = message
        uDispImgView.setImageDrawable(InfoManager.getUserImage(context))

        val imageDims = ceil(40 * InfoManager.getDpFactor(context)).toInt()
        uDispImgView.layoutParams = RelativeLayout.LayoutParams(imageDims, imageDims).apply {
            setPadding(5, 0, 5, 0)
            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        }

        if (sentByThisDevice) {
            addView(spacerView)
            infoContainer.addView(msgTextView)
            infoContainer.addView(uDispImgView)
        }
        else {
            infoContainer.addView(uDispImgView)
            infoContainer.addView(msgTextView)
            addView(spacerView)
        }

        addView(infoContainer)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        configureBySender()
        setSpacing()
    }

    /**
     * Sets the spacing of elements using LinearLayout weighting
     *
     */
    private fun setSpacing() {
        weightSum = 1f

        spacerView.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 0.15f
        }

        (infoContainer.layoutParams as LinearLayout.LayoutParams).weight = 0.85f
    }

    private fun configureBySender() {
        if (sentByThisDevice) {
            // Assign an ID so we can use it for aligning
            uDispImgView.id = View.generateViewId()

            // Align to right and change color if sent by this device's user
            (uDispImgView.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            (msgTextView.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.LEFT_OF, uDispImgView.id)

            msgTextView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.lightBlue, null))
        }
        else {
            // Assign an ID so we can use it for aligning
            msgTextView.id = View.generateViewId()

            // Align to left and change color if sent by another user
            (msgTextView.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            (uDispImgView.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.RIGHT_OF, msgTextView.id)

            msgTextView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.offWhite, null))
        }
    }

    private fun configureLayoutParams() {
        infoContainer.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)

        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(15, 15, 15, 15)
        layoutParams = params
    }


}