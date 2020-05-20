package com.kadabra.agent.utilities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.RelativeLayout
import android.widget.Toast
import com.kadabra.agent.R
import android.app.Activity





object Alert {

    var mLoadingDialog: Dialog? = null


    fun showProgress(context: Context) {

        if(mLoadingDialog!=null)
        {
            mLoadingDialog?.dismiss()
            mLoadingDialog=null
        }
        mLoadingDialog = Dialog(context)
        mLoadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mLoadingDialog?.setContentView(com.kadabra.agent.R.layout.progress_bar)
        mLoadingDialog?.window!!.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        mLoadingDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mLoadingDialog?.setCancelable(false)
        mLoadingDialog?.show()
    }

    fun hideProgress() {
        if (mLoadingDialog != null) {
            mLoadingDialog?.dismiss()
            mLoadingDialog = null
        }
//        mLoadingDialog!!.hide()
    }

    fun showMessage( message: String) {
        Toast.makeText(AppController.getContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun showMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun showAlertMessage(context: Context, title: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(com.kadabra.agent.R.drawable.placeholder)
            .setPositiveButton("ok", null)
            .show()

    }

}