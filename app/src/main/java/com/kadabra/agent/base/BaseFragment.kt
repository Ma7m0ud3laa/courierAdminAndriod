package com.twoam.cartello.Utilities.Base

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.twoam.agent.R
import com.twoam.agent.utilities.Alert


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
open class BaseFragment : Fragment(), OnItemClick {



    lateinit var mLoadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onItemClicked(position: Int) {

    }

    fun showAlertDialouge(title: String,message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.ic_launcher_background)
            .setPositiveButton("ok", null)
            .show()
    }

    fun showDialogue() {
        mLoadingDialog.show()
    }

    //hide progress bar Dialogue
    fun hideDialogue() {
        mLoadingDialog.dismiss()
    }



}
