package com.kadabra.agent.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TicketData {
    var NoOfUnreadedNotifications = 0
    var simpleTicketmodels = ArrayList<Ticket>()
}
