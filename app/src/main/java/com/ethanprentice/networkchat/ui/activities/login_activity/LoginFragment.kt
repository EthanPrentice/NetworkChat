package com.ethanprentice.networkchat.ui.activities.login_activity

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo

import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.adt.UserInfo
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Intent
import android.widget.*
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.makeramen.roundedimageview.RoundedDrawable
import com.makeramen.roundedimageview.RoundedImageView
import java.io.InputStream


/**
 * [Fragment] for to handle logging the user in, for now logging in is just entering a display name
 *
 * @author Ethan Prentice
 */
class LoginFragment : Fragment() {
    private var listener: OnLoginListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (container == null) {
            return inflater.inflate(R.layout.fragment_login, container, false)
        }
        val layout = inflater.inflate(R.layout.fragment_login, container, false) as FrameLayout


        val userImgView = layout.findViewById<RoundedImageView>(R.id.login_profile_img)
        val nameInput = layout.findViewById<EditText>(R.id.login_name_input)
        val loginBtn = layout.findViewById<Button>(R.id.login_btn)
        val rememberMeCheckBox = layout.findViewById<CheckBox>(R.id.login_remember_me_checkbox)

        fun Button.disable() {
            setBackgroundColor(resources.getColor(R.color.grey2, null))
            isClickable = false
        }

        fun Button.enable() {
            setBackgroundColor(resources.getColor(R.color.grey1, null))
            isClickable = true
        }

        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) { }

            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (str != null && str.length in 3..20) {
                    loginBtn.enable()
                }
                else {
                    loginBtn.disable()
                }
            }
        })

        nameInput.setOnEditorActionListener { view, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val nameText = nameInput.text.toString()
                if (nameText.length in 3..20) {
                    val uInfo = UserInfo(nameInput.text.toString(), null)

                    // If user checked "remember me" save the image and display name to cache
                    if (rememberMeCheckBox.isChecked) {
                        rememberUserInfo(uInfo)
                    }

                    listener?.onLogin(uInfo)
                }
            }
            true
        }

        loginBtn.setOnClickListener {
            val uInfo = UserInfo(nameInput.text.toString(), null)

            // If user checked "remember me" save the image and display name to cache
            if (rememberMeCheckBox.isChecked) {
                rememberUserInfo(uInfo)
            }

            listener?.onLogin(uInfo)
        }

        userImgView.setOnClickListener {
            selectUserImage()
        }

        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            val userImgView = it.findViewById<RoundedImageView>(R.id.login_profile_img)
            if (savedInstanceState != null) {
                val userBmp = savedInstanceState.getParcelable<Bitmap>("userImg")
                userImgView.invalidate()
                userImgView.setImageDrawable(null)
                userImgView.setImageBitmap(userBmp)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnLoginListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // No image was selected
                return
            }

            // parse selected image
            data.data?.let {
                val inputStream = context?.contentResolver?.openInputStream(it)
                if (inputStream != null) {
                    onUserImageSelected(inputStream)
                }
            }
        }
    }

    interface OnLoginListener {
        fun onLogin(userInfo: UserInfo)
    }


    private fun selectUserImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }

    private fun onUserImageSelected(inStream: InputStream) {
        activity?.let {
            val bmp = BitmapDrawable(resources, inStream)
            val userImgView = it.findViewById<RoundedImageView>(R.id.login_profile_img)
            userImgView.setImageDrawable(bmp)
        }
    }

    // TODO: replace this with some sort of CacheManager when it is implemented
    private fun rememberUserInfo(uInfo: UserInfo) {
        // Save image
        try {
            val cachePath = context?.cacheDir?.absolutePath
            val filename = "user_disp_img"
            val file = File(cachePath, filename)

            activity?.let {
                FileOutputStream(file).use { out ->
                    val userImgView = it.findViewById<RoundedImageView>(R.id.login_profile_img)
                    val bmp = (userImgView.drawable as RoundedDrawable).sourceBitmap
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val sharedPrefs = it.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
                with (sharedPrefs.edit()) {
                    putBoolean("rememberUser", true)
                    putString("userDisplayName", uInfo.displayName)
                    commit()
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()

        private const val TAG = "LoginFragment"

        private const val PICK_IMAGE = 1
    }
}
