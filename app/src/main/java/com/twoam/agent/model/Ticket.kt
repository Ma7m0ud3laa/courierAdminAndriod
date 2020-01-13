package com.twoam.agent.model

class Ticket {
    var title: String? = null
    var userName: String? = null
    var description: String? = null
    var pickUpLocation: PickUpLocation = PickUpLocation()
    var dropOffLocation: DropOffLocation = DropOffLocation()
    var stopList: ArrayList<Stop>? = null
    var status=""


    constructor() {}

    constructor(userName: String, title: String, description: String,status:String, pickUpLocation: PickUpLocation, dropOffLocation: DropOffLocation, stopList: ArrayList<Stop>) {
        this.userName = userName
        this.title = title
        this.description = description
        this.status=status
        this.pickUpLocation = pickUpLocation
        this.dropOffLocation = dropOffLocation
        this.stopList = stopList
    }

}
