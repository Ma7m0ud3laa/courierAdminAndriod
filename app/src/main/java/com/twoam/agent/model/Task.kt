package com.twoam.agent.model

class Task {
    var TaskId: String = ""
    var TicketId: String = ""
    var Task: String = ""
    var TaskDescription: String = ""
    var CourierId: Int = 0
    var CourierName: String = ""
    var Amount = 0.0
    var title: String? = null
    var stopsmodel = ArrayList<Stop>()
    var stopPickUp = Stop()
    var stopDropOff = Stop()
    var defaultStops = ArrayList<Stop>()
    var AddedBy: String = ""


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
        this.CourierId=courierId
        this.stopsmodel = stopList
    }

}
