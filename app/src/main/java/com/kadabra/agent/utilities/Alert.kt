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


object Alert {

    lateinit var mLoadingDialog: Dialog

    fun showProgress(context: Context) {
        mLoadingDialog = Dialog(context)
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mLoadingDialog.setContentView(R.layout.progress_bar)
        mLoadingDialog.window!!.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        mLoadingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mLoadingDialog.setCancelable(false)
        mLoadingDialog.show()
    }

    fun hideProgress() {
        mLoadingDialog.hide()
    }

    fun showMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

        fun showAlertMessage(context: Context, title: String, message: String) {
        AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher_background)
                .setPositiveButton("ok", null)
                .show()

    }

}