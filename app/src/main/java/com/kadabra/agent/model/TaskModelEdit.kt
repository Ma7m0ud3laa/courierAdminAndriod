package com.kadabra.agent.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TaskModelEdit {

    @SerializedName("TaskId")
    @Expose
    var taskId: String? = null
    @SerializedName("TaskName")
    @Expose
    var taskName: String? = null
    @SerializedName("TaskDescription")
    @Expose
    var TaskDescription: String? = null
    @Expose
    var amount: Double? = null
    @SerializedName("PickupTime")
    @Expose
    var pickupTime: String? = null
    @SerializedName("ModifiedBy")
    @Expose
    var modifiedBy: String? = null

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
        taskId: String,
        courierId: Int,
        stopList: ArrayList<Stopsmodel>
    ) {
        this.taskName = taskName
        this.amount = amount
        this.modifiedBy = addedBy
        this.ticketID = ticketId
        this.taskId=taskId
        this.courierId = courierId
        this.stopsmodels = stopList

    }

}
