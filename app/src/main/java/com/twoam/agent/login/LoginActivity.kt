package com.twoam.agent.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import com.reach.plus.admin.util.UserSessionManager
import com.twoam.Networking.INetworkCallBack
import com.twoam.Networking.NetworkManager

import com.twoam.agent.R
import com.twoam.agent.api.ApiResponse
import com.twoam.agent.api.ApiServices
import com.twoam.agent.model.User
import com.twoam.agent.ticket.TicketActivity
import com.twoam.agent.utilities.Alert
import com.twoam.agent.utilities.AppConstants
import kotlinx.android.synthetic.main.activity_login1.*

class LoginActivity : AppCompatActivity() {


    //region Members
    private var user: User = User()

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
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userName).matches()) {
            Alert.showMessage(this, getString(R.string.error_email))
            return false
        }
        if (etPassword.text.toString().trim().isNullOrEmpty()) {
            Alert.showMessage(this, getString(R.string.error_password))
            return false
        }
        return true
    }

    private fun logIn(userName: String, password: String): User {
        Alert.showProgress(this)
        if (NetworkManager().isNetworkAvailable(this)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.logIn(userName, password)
            NetworkManager().request(endPoint, object : INetworkCallBack<ApiResponse<User>> {
                override fun onFailed(error: String) {
                    Alert.hideProgress()
                    Alert.showMessage(this@LoginActivity, getString(R.string.no_internet))
                }

                override fun onSuccess(response: ApiResponse<User>) {
                    if (response.code == AppConstants.CODE_200) {
                        user = response.data!!
                        saveUserData(user!!)


                    } else {
                        Alert.hideProgress()
                        Alert.showMessage(this@LoginActivity, getString(R.string.error_email_password_incorrect))
                    }

                }
            })

        } else {
            Alert.hideProgress()
            Alert.showMessage(this@LoginActivity, getString(R.string.no_internet))
        }
        return user!!
    }

    private fun saveUserData(user: User) {
        UserSessionManager.getInstance(this).setUserData(user)
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
    //endregion
}
