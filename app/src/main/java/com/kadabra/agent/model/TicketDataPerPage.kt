package com.kadabra.agent.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TicketDataPerPage {
    var TotalPages = 0
    var RemainingPages = 0
    var ticketmodels = ArrayList<Ticket>()
}
