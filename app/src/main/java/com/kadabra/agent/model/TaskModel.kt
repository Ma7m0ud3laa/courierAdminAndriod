package com.kadabra.agent.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TaskModel {

    @SerializedName("TaskId")
    @Expose
    var taskId: String? = null
    @SerializedName("TaskName")
    @Expose
    var taskName: String? = null
    @SerializedName("TaskDescription")
    @Expose
    var TaskDescription: String? = null
    @SerializedName("Amount")
    @Expose
    var amount: Double? = null
    @SerializedName("PickUpTime")
    @Expose
    var pickupTime: String? = null
    @SerializedName("AddedBy")
    @Expose
    var addedBy: String? = null

    @SerializedName("TicketID")
    @Expose
    var ticketID: String? = null
    @SerializedName("CourierId")
    @Expose
    var courierId: Int? = null
    @SerializedName("stopsmodels")
    @Expose
    var stopsmodels= ArrayList<Stopsmodel>()
    @SerializedName("serviceCosts")
    @Expose
    var serviceCosts=  ArrayList<TicketServiceCost>()

    constructor()
    constructor(
        taskName: String,
        amount: Double,
        addedBy: String,
        ticketId: String,
        courierId: Int,
        stopList: ArrayList<Stopsmodel>,
        serviceCosts:ArrayList<TicketServiceCost>
    ) {
        this.taskName = taskName
        this.amount = amount
        this.addedBy = addedBy
        this.ticketID = ticketId
        this.courierId = courierId
        this.stopsmodels = stopList

    }

}
