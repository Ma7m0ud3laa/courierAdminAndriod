package com.twoam.agent.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.reach.plus.admin.util.UserSessionManager
import com.twoam.Networking.INetworkCallBack
import com.twoam.Networking.NetworkManager
import com.twoam.agent.R
import com.twoam.agent.api.ApiResponse
import com.twoam.agent.api.ApiServices
import com.twoam.agent.model.Admin
import com.twoam.agent.ticket.TicketActivity
import com.twoam.agent.utilities.Alert
import com.twoam.agent.utilities.AppConstants
import kotlinx.android.synthetic.main.activity_login1.*

class LoginActivity : AppCompatActivity() {


    //region Members
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
            var endPoint = request.logIn(usreName, password)
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
                        Alert.hideProgress()
                        admin = response.ResponseObj!!
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
        setContentView(R.layout.activity_login1)
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
    //endregion
}
