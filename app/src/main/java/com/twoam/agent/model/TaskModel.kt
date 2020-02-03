package com.twoam.agent.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TaskModel {

    @SerializedName("TaskId")
    @Expose
    var taskId: String? = null
    @SerializedName("TaskName")
    @Expose
    var taskName: String? = null
    @SerializedName("Amount")
    @Expose
    var amount: Double? = null
    @SerializedName("AddedBy")
    @Expose
    var addedBy: String? = null
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
        this.addedBy = addedBy
        this.ticketID = ticketId
        this.courierId = courierId
        this.stopsmodels = stopList

    }

}
