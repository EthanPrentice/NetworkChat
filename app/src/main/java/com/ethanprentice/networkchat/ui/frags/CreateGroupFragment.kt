package com.ethanprentice.networkchat.ui.frags

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import com.ethanprentice.networkchat.adt.GroupInfo
import android.view.inputmethod.InputMethodManager
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.activities.chat_activity.ui.chat.ChatFragment


/**
 *
 * @author Ethan Prentice
 */
class CreateGroupFragment : Fragment() {
    private var listener: CreateGroupFragListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (container == null) {
            return null
        }

        val layout = inflater.inflate(R.layout.fragment_create_group, container, false) as FrameLayout

        val gNameInput = layout.findViewById<EditText>(R.id.new_group_name_input)
        val createGroupBtn = layout.findViewById<Button>(R.id.create_group_btn)

        fun Button.disable() {
            setBackgroundColor(resources.getColor(R.color.grey, null))
            isClickable = false
        }

        fun Button.enable() {
            setBackgroundColor(resources.getColor(R.color.lightGrey, null))
            isClickable = true
        }

        gNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) { }

            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (str != null && 3 <= str.length && str.length <= 20) {
                    createGroupBtn.enable()
                }
                else {
                    createGroupBtn.disable()
                }
            }
        })

        gNameInput.setOnEditorActionListener { view, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                createGroup(gNameInput)
            }
            true
        }

        createGroupBtn.setOnClickListener {
            createGroup(gNameInput)
        }

        return layout
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
