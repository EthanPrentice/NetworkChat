package com.ethanprentice.networkchat.ui.frags

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.ethanprentice.networkchat.adt.GroupInfo
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.ui.activities.chat_activity.ChatActivity
import java.lang.IllegalStateException
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.ceil


/**
 *
 * @author Ethan Prentice
 */
class CreateGroupFragment : DialogFragment() {
    private var listener: CreateGroupFragListener? = null

    // set color to grey and show error message on click
    private fun Button.disable() {
        setTextColor(resources.getColor(R.color.grey2, null))
        setOnClickListener {
            val toast = Toast.makeText(context, "Group userDispName must be between 3 and 20 characters", Toast.LENGTH_LONG)
            toast.view.background.setColorFilter(resources.getColor(R.color.grey1, null), PorterDuff.Mode.SRC_IN)
            toast.show()
        }
    }

    // set color to normal and create group on click
    private fun Button.enable(layout: View) {
        setTextColor(resources.getColor(R.color.colorPrimaryDark, null))
        setOnClickListener {
            createGroup(layout.findViewById<EditText>(R.id.new_group_name_input))
            val toast = Toast.makeText(context, "Created group", Toast.LENGTH_SHORT)
            toast.view.background.setColorFilter(resources.getColor(R.color.grey1, null), PorterDuff.Mode.SRC_IN)
            toast.show()
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(context)
            val inflater = requireActivity().layoutInflater

            val layout = inflater.inflate(R.layout.dialog_create_group, null) as ConstraintLayout

            builder.setView(layout)

                    .setPositiveButton("Create Group", null)
                    .setNegativeButton("Cancel") { _, _ -> dialog.cancel() }

            val dialog = builder.create()
            addListeners(layout, dialog)

            dialog
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

            // change background color
            val bg = resources.getDrawable(R.drawable.rounded_rect_bg, null)
            bg.mutate().setColorFilter(resources.getColor(R.color.colorPrimary, null), PorterDuff.Mode.MULTIPLY)
            dialog.window?.setBackgroundDrawable(bg)

            // disable positive button
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).disable()
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
        val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveBtn.background = ColorDrawable(Color.TRANSPARENT)
        negativeBtn.background = ColorDrawable(Color.TRANSPARENT)

        positiveBtn.setTextColor(resources.getColor(R.color.colorPrimaryDark, null))
        negativeBtn.setTextColor(resources.getColor(R.color.colorPrimaryDark, null))

        val positiveButtonLL = positiveBtn.layoutParams as LinearLayout.LayoutParams
        positiveButtonLL.gravity = Gravity.CENTER
        positiveBtn.layoutParams = positiveButtonLL

        val negativeButtonLL = negativeBtn.layoutParams as LinearLayout.LayoutParams
        negativeButtonLL.gravity = Gravity.CENTER
        negativeBtn.layoutParams = negativeButtonLL
    }

    private fun addListeners(layout: View, dialog: AlertDialog) {
        val gNameInput = layout.findViewById<EditText>(R.id.new_group_name_input)

        gNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) { }

            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (str != null && 3 <= str.length && str.length <= 20) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).enable(layout)
                }
                else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).disable()
                }
            }
        })

        gNameInput.setOnEditorActionListener { view, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                createGroup(gNameInput)
            }
            true
        }
    }


    private fun createGroup(gNameInput: EditText) {
        val gInfo = GroupInfo(gNameInput.text.toString())
        gNameInput.text?.clear()
        listener?.onCreateGroup(this, gInfo)

        // hide keyboard
        activity?.let {
            if (it is ChatActivity) {
                it.hideKeyboard()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CreateGroupFragListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CreateGroupFragListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface CreateGroupFragListener {
        fun onCreateGroup(frag: CreateGroupFragment, gInfo: GroupInfo)
    }

    companion object {
        @JvmStatic
        fun newInstance() = CreateGroupFragment()
    }
}
