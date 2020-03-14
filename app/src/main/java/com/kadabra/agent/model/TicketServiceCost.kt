package com.kadabra.agent.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TicketServiceCost(
    @SerializedName("SeviceCostName")
    @Expose
    val serviceCostName: String,
    @SerializedName("Cost")
    @Expose
    val cost: Double)