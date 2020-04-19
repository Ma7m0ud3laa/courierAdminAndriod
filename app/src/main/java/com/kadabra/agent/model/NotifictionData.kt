package com.kadabra.agent.model

import android.app.Notification
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class NotifictionData {
    var NoOfUnreadedNotifications = 0
    var adminNotificationModels = ArrayList<Notification>()
}
