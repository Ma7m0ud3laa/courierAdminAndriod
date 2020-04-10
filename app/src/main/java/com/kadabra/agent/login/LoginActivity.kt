package com.kadabra.agent.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.reach.plus.admin.util.UserSessionManager
import com.kadabra.Networking.INetworkCallBack
import com.kadabra.Networking.NetworkManager
import com.kadabra.agent.R
import com.kadabra.agent.api.ApiResponse
import com.kadabra.agent.api.ApiServices
import com.kadabra.agent.firebase.FirebaseManager
import com.kadabra.agent.model.Admin
import com.kadabra.agent.ticket.TicketActivity
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AppConstants
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {


    //region Members
    private var TAG=this.javaClass.simpleName
    private var admin: Admin = Admin()

    //endregion
    //region Helper Functions
    private fun init() {



        btnLogIn.setOnClickListener {

            if (validateData()) {
                var userName = etUserName.text.toString()
                var password = etPassword.text.toString()

                logIn(userName, password)
            }
        }
    }

    private fun validateData(): Boolean {
        var userName = etUserName.text.toString().trim()
        if (userName.isNullOrEmpty()) {
            Alert.showMessage(this, getString(R.string.error_user_name))
            return false
        }
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(courierName).matches()) {
//            Alert.showMessage(this, getString(R.string.error_email))
//            return false
//        }
        if (etPassword.text.toString().trim().isNullOrEmpty()) {
            Alert.showMessage(this, getString(R.string.error_password))
            return false
        }
        return true
    }

    private fun logIn(usreName: String, password: String): Admin {
        Alert.showProgress(this)
//        showProgress()
        if (NetworkManager().isNetworkAvailable(this)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.logIn(usreName, password,1)
            NetworkManager().request(endPoint, object : INetworkCallBack<ApiResponse<Admin>> {
                override fun onFailed(error: String) {
                    Alert.hideProgress()
                    Alert.showMessage(
                        this@LoginActivity,
                        getString(R.string.error_login_server_error)
                    )
                }

                override fun onSuccess(response: ApiResponse<Admin>) {
                    if (response.Status == AppConstants.STATUS_SUCCESS) {
                        admin = response.ResponseObj!!
                        sendUserToken(admin.AdminId, FirebaseManager.token)
                        Alert.hideProgress()

                        saveUserData(admin)


                    } else if (response.Status == AppConstants.STATUS_FAILED) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            this@LoginActivity,
                            getString(R.string.error_incorrect_user_name)
                        )
                    } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            this@LoginActivity,
                            getString(R.string.error_incorrect_password)
                        )
                    }

                }
            })

        } else {
            Alert.hideProgress()
            Alert.showMessage(this@LoginActivity, getString(R.string.no_internet))
        }
        return admin
    }

    private fun sendUserToken(id: String, token: String) {
        if (NetworkManager().isNetworkAvailable(this)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.setAdminToken(id, token)
            NetworkManager().request(endPoint, object : INetworkCallBack<ApiResponse<Boolean>> {
                override fun onSuccess(response: ApiResponse<Boolean>) {
                    Log.d(TAG, "SEND TOKEN - API - SUCCESSFULLY.")
                }

                override fun onFailed(error: String) {
                    Log.d(TAG, "SEND TOKEN - API - FAILED.")
                    Alert.showMessage(
                        this@LoginActivity,
                        getString(R.string.error_login_server_error)
                    )
                }
            })


        } else {
            Log.d(TAG, "SEND TOKEN - API - NO INTERNET.")
            Alert.showMessage(this@LoginActivity, getString(R.string.no_internet))
        }

    }

    private fun saveUserData(admin: Admin) {
        AppConstants.CurrentLoginAdmin = admin
        UserSessionManager.getInstance(this).setUserData(admin)
        UserSessionManager.getInstance(this).setIsLogined(true)
        startActivity(Intent(this, TicketActivity::class.java))
        finish()

    }
    //endregion

    //region Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
    }

    private fun showProgress() {
        avi.visibility= View.VISIBLE
        avi.smoothToShow()
    }

    private fun hideProgress() {
        avi.smoothToHide()
    }

    override fun onDestroy() {
        super.onDestroy()
        Alert.hideProgress()
    }
    //endregion
}
