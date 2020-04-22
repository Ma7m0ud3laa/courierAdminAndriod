package com.kadabra.agent.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class NotificationData {
    var adminNotificationModels: ArrayList<Notification>? = null
    var NoOfUnreadedNotifications:Int=0

}
