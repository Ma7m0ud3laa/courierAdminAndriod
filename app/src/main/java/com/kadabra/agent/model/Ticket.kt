package com.twoam.agent.model

class Ticket {
    var TicketId: String = ""
    var AdminId: String = ""
    var AdminName: String = ""
    var TicketName: String = ""
    var TicketDescription: String = ""
    var PickUpName: String = ""
    var PickUpLongitude: Double? = null
    var PickUpLatitude: Double? = null
    var DropOffName: String = ""
    var DropOffLongitude: Double? = null
    var DropOffLatitude: Double? = null
    var Status: String = ""
    var price: Double? = null
    var Priority: String = ""

    var taskModel = ArrayList<Task>()
    var title: String? = null
    var pickUpLocation: PickUpLocation = PickUpLocation()//need to be with AdminName
    var dropOffLocation: DropOffLocation = DropOffLocation()//need to be with AdminName
    var stopList: ArrayList<Stop>? = null//need to be added and with AdminName


    constructor() {}

    constructor(id: String, name: String) {
        this.TicketId = id
        this.TicketName = name
    }
    constructor(
        adminName: String,
        title: String,
        description: String,
        taskStatus: String,
        pickUpLocation: PickUpLocation,
        dropOffLocation: DropOffLocation,
        stopList: ArrayList<Stop>
    ) {
        this.AdminName = adminName
        this.title = title
        this.TicketDescription = description
        this.Status = taskStatus
        this.pickUpLocation = pickUpLocation
        this.dropOffLocation = dropOffLocation
        this.stopList = stopList
    }

    constructor(
        ticketId: String,
        AdminId: String,
        TicketName: String,
        TicketDescription: String,
        PickUpName: String,
        PickUpLongitude: Double?,
        PickUpLatitude: Double?,
        DropOffName: String,
        DropOffLongitude: Double?,
        DropOffLatitude: Double?,
        taskStatus: String,
        tasks: ArrayList<Task>,
        title: String?,
        pickUpLocation: PickUpLocation,
        dropOffLocation: DropOffLocation,
        stopList: ArrayList<Stop>?
    ) {
        this.TicketId = ticketId
        this.AdminId = AdminId
        this.TicketName = TicketName
        this.TicketDescription = TicketDescription
        this.PickUpName = PickUpName
        this.PickUpLongitude = PickUpLongitude
        this.PickUpLatitude = PickUpLatitude
        this.DropOffName = DropOffName
        this.DropOffLongitude = DropOffLongitude
        this.DropOffLatitude = DropOffLatitude
        this.Status = taskStatus
        this.taskModel = tasks
        this.title = title
        this.pickUpLocation = pickUpLocation
        this.dropOffLocation = dropOffLocation
        this.stopList = stopList
    }


}
