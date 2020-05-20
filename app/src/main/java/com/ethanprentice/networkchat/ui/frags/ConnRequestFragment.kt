package com.ethanprentice.networkchat.ui.frags

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.ethanprentice.networkchat.R
import java.lang.IllegalStateException
import kotlin.math.ceil



class ConnRequestFragment : DialogFragment() {

    private var listener: ConnRequestFragListener? = null

    private lateinit var uDispName: String
    private lateinit var userIp: String
    private var userPort: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            uDispName = it.getString("uDispName")!!
            userIp = it.getString("userIp")!!
            userPort = it.getInt("userPort")
        }

        savedInstanceState?.let {
            uDispName = it.getString("uDispName")!!
            userIp = it.getString("userIp")!!
            userPort = it.getInt("userPort")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.DefaultDialogStyle))
            val inflater = requireActivity().layoutInflater

            val layout = inflater.inflate(R.layout.dialog_conn_req, null) as ConstraintLayout

            val textView = layout.findViewById<TextView>(R.id.conn_req_dialog_text)
            textView.text = Html.fromHtml("<b>%s</b> would like to join your group!".format(uDispName))

            builder.setView(layout)
                    .setPositiveButton("Accept") { _, _ -> listener?.onConnReqAccepted(this, userIp, userPort) }
                    .setNeutralButton("Ignore") { _, _ -> listener?.onConnReqIgnored(this, userIp, userPort) }
                    .setNegativeButton("Block") { _, _ ->
                        listener?.onConnReqBlocked(this, userIp, userPort)
                        dialog.cancel()
                    }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null!")
    }


    override fun onStart() {
        super.onStart()
        dialog.window?.setWindowAnimations(R.style.VerticalSlideDialogAnimation)

        if (dialog is AlertDialog) {
            val alertDialog = dialog as AlertDialog

            placeButtons(alertDialog)
            setDimensions(alertDialog)

            // scroll open when typing in dialog
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }



    private fun setDimensions(dialog: AlertDialog) {
        val lp = WindowManager.LayoutParams()

        dialog.window?.let {
            lp.copyFrom(dialog.window?.attributes)

            val sideMargins = 20
            val newWidthPx = 200 + 2 * sideMargins
            val newWidthDp = ceil(newWidthPx * resources.displayMetrics.density).toInt()

            lp.width = newWidthDp
            it.attributes = lp
        }
    }

    private fun placeButtons(dialog: AlertDialog) {
        val acceptBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val ignoreBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        val blockBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        fun Button.placeButton() {
            background = ColorDrawable(Color.TRANSPARENT)
            setTextColor(resources.getColor(R.color.colorPrimaryDark, null))

            (layoutParams as LinearLayout.LayoutParams).apply {
                gravity = Gravity.CENTER
            }
        }

        acceptBtn.placeButton()
        ignoreBtn.placeButton()
        blockBtn.placeButton()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConnRequestFragListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ConnRequestFragListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface ConnRequestFragListener {
        fun onConnReqAccepted(frag: ConnRequestFragment, userIp: String, userPort: Int)
        fun onConnReqIgnored(frag: ConnRequestFragment, userIp: String, userPort: Int)
        fun onConnReqBlocked(frag: ConnRequestFragment, userIp: String, userPort: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance(uDispName: String, userIp: String, userPort: Int): ConnRequestFragment {
            val bundle = Bundle()
            bundle.putString("uDispName", uDispName)
            bundle.putString("userIp", userIp)
            bundle.putInt("userPort", userPort)

            val frag = ConnRequestFragment()
            frag.arguments = bundle

            return frag
        }
    }

}