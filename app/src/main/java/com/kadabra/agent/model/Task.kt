package com.kadabra.agent.model

import com.google.firebase.database.Exclude
import java.util.*
import kotlin.collections.ArrayList

class Task {

    @Exclude
    @set:Exclude
    @get:Exclude
    var TaskId: String = ""
    @Exclude
    @set:Exclude
    @get:Exclude
    var TicketId: String = ""
    @Exclude
    @set:Exclude
    @get:Exclude
    var TaskName: String = ""
    @Exclude
    @set:Exclude
    @get:Exclude
    var TaskDescription: String? = null
    var CourierID: Int? = null
    @Exclude
    @set:Exclude
    @get:Exclude
    var CourierName: String = ""
    @Exclude
    @set:Exclude
    @get:Exclude
    var Amount = 0.0
    var PickUpTime=""

    @Exclude
    @set:Exclude
    @get:Exclude
    var title: String? = null
    @Exclude
    @set:Exclude
    @get:Exclude
    var stopsmodel = ArrayList<Stop>()
    @Exclude
    @set:Exclude
    @get:Exclude
    var stopPickUp = Stop()
    @Exclude
    @set:Exclude
    @get:Exclude
    var stopDropOff = Stop()
    @Exclude
    @set:Exclude
    @get:Exclude
    var defaultStops = ArrayList<Stop>()
    @Exclude
    @set:Exclude
    @get:Exclude
    var AddedBy: String = ""
    @Exclude
    @set:Exclude
    @get:Exclude
    lateinit var location: location
    var isActive: Boolean = false
    @set:Exclude
    @get:Exclude
    var Status = ""
    @set:Exclude
    @get:Exclude
    var serviceCosts = ArrayList<TicketServiceCost>()


    constructor() {}

    constructor(
        taskName: String,
        taskDescription: String,
        amount: Double,
        pickUpTime: String,
        addedBy: String,
        ticketId: String,
        taskId: String,
        courierId: Int,
        stopList: ArrayList<Stop>,
        serviceCostList: ArrayList<TicketServiceCost>

    ) {
        this.TaskName = taskName
        this.TaskDescription = taskDescription
        this.Amount = amount
        this.PickUpTime=pickUpTime
        this.AddedBy = addedBy
        this.TicketId = ticketId
        this.TaskId = taskId
        this.CourierID = courierId
        this.stopsmodel = stopList
        this.serviceCosts=serviceCostList
    }

}
