package com.twoam.agent.model

import com.google.firebase.database.Exclude

class Task {
//    var TaskId: String = ""
//    var TicketId: String = ""
//    var Task: String = ""
//    var TaskDescription: String = ""
//    var CourierID: Int ? = null
//    var name: String = ""
//    var Amount = 0.0
//    var title: String? = null
//    var stopsmodel = ArrayList<Stop>()
//    var stopPickUp = Stop()
//    var stopDropOff = Stop()
//    var defaultStops = ArrayList<Stop>()
//    var AddedBy: String = ""
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
    var Task: String = ""
    @Exclude
    @set:Exclude
    @get:Exclude
    var TaskDescription: String = ""
    var CourierID: Int ? = null
    @Exclude
    @set:Exclude
    @get:Exclude
    var CourierName: String = ""
    @Exclude
    @set:Exclude
    @get:Exclude
    var Amount = 0.0
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



    constructor() {}

    constructor(
        taskName: String,
        amount: Double,
        addedBy: String,
        ticketId: String,
        taskId: String,
        courierId: Int,
        stopList: ArrayList<Stop>
    ) {
        this.Task = taskName
        this.Amount = amount
        this.AddedBy = addedBy
        this.TicketId = ticketId
        this.TaskId = taskId
        this.CourierID = courierId
        this.stopsmodel = stopList
    }

}
