package com.twoam.agent.intro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.reach.plus.admin.util.UserSessionManager
import com.twoam.Networking.INetworkCallBack
import com.twoam.Networking.NetworkManager
import com.twoam.agent.ticket.TicketActivity

import com.twoam.agent.R
import com.twoam.agent.adapter.TicketAdapter
import com.twoam.agent.api.ApiResponse
import com.twoam.agent.api.ApiServices
import com.twoam.agent.exception.CrashActivity
import com.twoam.agent.exception.CrashHandeller
import com.twoam.agent.login.LoginActivity
import com.twoam.agent.model.Ticket
import com.twoam.agent.utilities.Alert
import com.twoam.agent.utilities.AppConstants
import com.twoam.agent.utilities.AppController
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.fragment_ticket.*

class SplashActivity : AppCompatActivity() {


    //region Members
    private var startTime: Long = 2500

    //endregion
//region Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        init()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    callIntent()
                } else {
                    checkLocationPermission()
                }
                return
            }
        }
    }

    //endregion
//region Helper Functions
    private fun init() {

        CrashHandeller.deploy(this, CrashActivity::class.java)
        rippleBackground.startRippleAnimation()

        Handler().postDelayed({
            if (UserSessionManager.getInstance(this).getUserData() == null) {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            } else {
                AppConstants.CurrentLoginAdmin =
                    UserSessionManager.getInstance(this).getUserData()!!
                startActivity(Intent(this@SplashActivity, TicketActivity::class.java))
            }

            finish()
        }, startTime)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        } else {

        }
    }

    //endregion
}
