package com.kadabra.agent.callback

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import com.reach.plus.admin.util.UserSessionManager
import com.kadabra.agent.R
import com.kadabra.agent.login.LoginActivity
import com.kadabra.agent.utilities.AppConstants
import com.kadabra.agent.utilities.AppController


class BottomSheetNavigationFragment : BottomSheetDialogFragment() {


    private var closeButton: ImageView? = null
    private var listener: IBottomSheetCallback? = null
    //Bottom Sheet Callback
    private val mBottomSheetBehaviorCallback =
        object : BottomSheetBehavior.BottomSheetCallback(), IBottomSheetCallback {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //check the slide offset and change the visibility of close button
                if (slideOffset > 0.5) {
                    closeButton!!.visibility = View.VISIBLE
                } else {
                    closeButton!!.visibility = View.GONE
                }
            }

            override fun onBottomSheetClosed(isClosed: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onBottomSheetSelectedItem(index: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        } else {
            throw ClassCastException("$context must implement IBottomSheetCallback.onBottomSheetSelectedItem")
        }
    }


    override fun onDetach() {
        super.onDetach()
//        listener = null
    }


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        //Get the content View
        val contentView = View.inflate(context, R.layout.bottom_navigation_drawer, null)
        dialog.setContentView(contentView)

        val navigationView = contentView.findViewById<NavigationView>(R.id.navigation_view)

        //implement navigation menu item click event
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navLogout -> {

                    AlertDialog.Builder(context)
                        .setTitle(AppConstants.WARNING)
                        .setMessage(getString(R.string.exit))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(AppConstants.OK) { dialog, which ->
                            logOut()
                        }
                        .setNegativeButton(AppConstants.CANCEL) { dialog, which -> }
                        .show()

                }
                R.id.navCall -> {
                    val intent = Intent(Intent.ACTION_DIAL)
                    startActivity(intent)
                    dismiss()
                }
                R.id.navTickets -> {
                    listener!!.onBottomSheetSelectedItem(11)
                    dismiss()
                }

            }
            dismiss()
            false
        }
        closeButton = contentView.findViewById(R.id.close_image_view)
        closeButton!!.setOnClickListener {
            //dismiss bottom sheet
            dismiss()
        }

        //Set the coordinator layout behavior
        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior

        //Set callback
        if (behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
    }

    private fun logOut() {
        UserSessionManager.getInstance(AppController.getContext()).setUserData(null)
        UserSessionManager.getInstance(AppController.getContext()).setIsLogined(false)
        listener!!.onBottomSheetSelectedItem(14)
        dismiss()
    }

    companion object {


        fun newInstance(): BottomSheetNavigationFragment {

            val args = Bundle()

            val fragment = BottomSheetNavigationFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
