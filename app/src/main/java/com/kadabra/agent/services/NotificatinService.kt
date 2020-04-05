package com.kadabra.agent.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kadabra.Networking.INetworkCallBack
import com.kadabra.Networking.NetworkManager
import com.kadabra.agent.R
import com.kadabra.agent.api.ApiResponse
import com.kadabra.agent.api.ApiServices
import com.kadabra.agent.login.LoginActivity
import com.kadabra.agent.ticket.TicketActivity
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AppConstants
import com.kadabra.agent.utilities.AppController
import com.reach.plus.admin.util.UserSessionManager



class NotificatinService : FirebaseMessagingService() {

    var TAG = "NotificatinService"

    var admin = UserSessionManager.getInstance(
        this
    ).getUserData()


    override fun onNewToken(s: String) {
        super.onNewToken(s)
//        sendToken(s)
    }

    fun sendToken(token: String) {

        if (admin!=null&&!admin?.AdminId.isNullOrEmpty()) {
            sendUserToken(admin!!.AdminId, token)
        }

    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        val map = remoteMessage.data
        var title = ""
        var message = ""

        if (map != null) {
            title = map["title"].toString()
            message = map["body"].toString()
        }

        Log.d(
            TAG, "onMessageReceived: Message Received: \n" +
                    "Title: " + title + "\n" +
                    "Message: " + message
        )

        if (message == "LogOut") {
            AppConstants.FIRE_BASE_LOGOUT = true

            UserSessionManager.getInstance(AppController.getContext()).logout()
            startActivity(
                Intent(
                    AppController.getContext(),
                    LoginActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
        else if (title == "acceptTask") {
//            AppConstants.FIRE_BASE_NEW_TASK = true

            Log.d(
                TAG, "$title: \n" +
                        AppConstants.FIRE_BASE_NEW_TASK + "\n" +
                        "admin: " + admin?.AdminId
            )
//            if (admin != null && !admin?.AdminId.isNullOrEmpty()) {
                var intent = Intent(
                    AppController.getContext(),
                    TicketActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
//                startActivity(
//                    intent
//                )
                sendNotification(title!!, message, intent)
//
//                Log.d(TAG, "Sended")
//            } else // user didn't log in yet
//            {
//                Log.d(TAG, "Not Login yet")
//                var intent = Intent(
//                    AppController.getContext(),
//                    LoginActivity::class.java
//                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    .setAction(Intent.ACTION_MAIN)
//                    .addCategory(Intent.CATEGORY_LAUNCHER)
//                startActivity(
//                    intent
//                )
//                sendNotification(title!!, message, intent)
//            }


        } else if (title == "endTask") {
            AppConstants.FIRE_BASE_EDIT_TASK = true

            Log.d(
                TAG, "$title: \n" +
                        AppConstants.FIRE_BASE_EDIT_TASK + "\n" +
                        "admin: " + admin?.AdminId
            )
//            if (admin != null && !admin?.AdminId.isNullOrEmpty()) {
                var intent = Intent(
                    AppController.getContext(),
                    TicketActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
//                startActivity(
//                    intent
//                )
                sendNotification(title!!, message, intent)
//
//                Log.d(TAG, "Sended")
//            } else // user didn't log in yet
//            {
//                Log.d(TAG, "Not Login yet")
//                var intent = Intent(
//                    AppController.getContext(),
//                    LoginActivity::class.java
//                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    .setAction(Intent.ACTION_MAIN)
//                    .addCategory(Intent.CATEGORY_LAUNCHER)
//                startActivity(
//                    intent
//                )
//                sendNotification(title!!, message, intent)
//            }


        } else {
            Log.d(TAG, "DEFAULT")
            val intent = Intent(this, TicketActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            sendNotification(title!!, message, intent)

        }


    }

    private fun sendNotification(title: String?, message: String?, intent: Intent) {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "123456"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                AppConstants.ADMIN_CHANNEL_ID,
                AppConstants.ADMIN_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Configure the notification channel.
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED

            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // assuming your main activity
        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//        val intent = Intent(this, TaskActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setOngoing(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("admin")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentTitle(title)
            .setContentText(message)
            .setFullScreenIntent(pendingIntent, true)
            .setContentInfo("Info")
            .setUsesChronometer(false)


        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }


    private fun newTaskNotification(title: String, message: String, intent: Intent) {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "123456"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                AppConstants.ADMIN_CHANNEL_ID,
                AppConstants.ADMIN_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Configure the notification channel.
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED

            notificationChannel.vibrationPattern = longArrayOf(
                500,
                500,
                500,
                500,
                500,
                500,
                500,
                500,
                500,
                500,
                500,
                500,
                500,
                500
            )
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // assuming your main activity
        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//        val intent = Intent(this, TaskActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setOngoing(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("admin")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentTitle(title)
            .setContentText(message)
            .setFullScreenIntent(pendingIntent, true)
            .setContentInfo("Info")
            .setUsesChronometer(false)


        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())

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
                        AppController.getContext(),
                        getString(R.string.error_login_server_error)
                    )
                }
            })


        } else {
            Log.d(TAG, "SEND TOKEN - API - NO INTERNET.")
            Alert.showMessage(AppController.getContext(), getString(R.string.no_internet))
        }

    }

    companion object {

        private val TAG = NotificatinService::class.java.name
    }
}
