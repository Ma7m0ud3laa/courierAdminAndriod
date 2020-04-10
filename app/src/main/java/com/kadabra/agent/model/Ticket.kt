package com.kadabra.agent.model

class Ticket {
    var TicketId: String? = null
    var AdminId: String = ""
    var AdminName: String? = null
    var TicketName: String = ""
    var TicketDescription: String = ""
    var UserMobile: String = ""
    var TotalTasks = ""
    var TotalTasksAmount=""
    var PickUpName: String = ""

    var PickUpLongitude: Double? = null

    var PickUpLatitude: Double? = null

    var DropOffName: String = ""

    var DropOffLongitude: Double? = null

    var DropOffLatitude: Double? = null
    var StatusId: Int? = null

    var Status: String? = null
    var CategoryId: String = ""

    var Category: String = ""

    var price: Double? = null
    var PriorityId: Int? = null

    var Priority: String? = null
    var PaymentMethodId: Int? = null
    var PaymentMethod: String? = null
    var NeedCourier = false

    var taskModel = ArrayList<Task>()

    var title: String? = null

    var pickUpLocation: PickUpLocation = PickUpLocation()//need to be with AdminName

    var dropOffLocation: DropOffLocation = DropOffLocation()//need to be with AdminName

    var stopList: ArrayList<Stop>? = null//need to be added and with AdminName
    var ticketCategories: ArrayList<TicketCategory>? = null
    var ticketStatus: ArrayList<TicketStatus>? = null
    var ticketPriority: ArrayList<TicketPriority>? = null
    var ticketPaymetMethods: ArrayList<TicketPaymentMethod>? = null
    var serviceCosts = ArrayList<TicketServiceCost>()


    constructor() {}

    constructor(id: String, name: String) {
        this.TicketId = id
        this.TicketName = name
    }

    constructor(
        id: String,
        name: String,
        ticketDescription: String,
        userMobile: String,
        categoryId: String,
        priorityId: Int,
        statusId: Int,
        paymentId: Int,
        needCourier: Boolean,
        serviceCostList: ArrayList<TicketServiceCost>,
        adminId: String
    ) {
        this.TicketId = id
        this.TicketName = name
        this.TicketDescription = ticketDescription
        this.UserMobile = userMobile
        this.CategoryId = categoryId
        this.PriorityId = priorityId
        this.StatusId = statusId
        this.PaymentMethodId = paymentId
        this.NeedCourier = needCourier
        this.serviceCosts = serviceCostList
        this.AdminId = adminId
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
