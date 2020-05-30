package com.kadabra.agent.model

class Courier {
    var CourierId: Int? = null
    var CourierName: String = ""
    var name: String = ""
    var isActive = false
    var location = location()
    var token: String = ""
    var CourierMobile: String = ""
    var HasTasksNow = false
    var HasTasksWithinHour = false
    var VehicleTypeID = 1  //1 car 2 bike
    var TaskId = ""
    var PickupName = ""
    var DropoffName = ""
    var TreasuryValue = 0.0

    constructor()
    constructor(id: Int?, name: String) {
        this.CourierId = id
        this.name = name
    }
}