package com.kadabra.cartello.Utilities.Base

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.kadabra.agent.R


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
