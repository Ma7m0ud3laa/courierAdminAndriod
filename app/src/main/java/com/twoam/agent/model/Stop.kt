package com.twoam.agent.model

class Stop {
    var id: String = ""
    var StopName: String = ""
    var Latitude: Double? = null
    var Longitude: Double? = null
    var StopTypeID: Int = 0 //1 pickup 2 dropoff 3 stop "default"
    var StopType = ""
    var CreationDate = ""
    var taskId = ""
    var addedBy = ""
    var address = ""
    var city = ""
    var state = ""
    var country = ""
    var postalCode = ""
    var knownName = ""


    constructor()
    constructor(
        taskId: String, addedBy: String,
        stopName: String, latitude: Double
        , longitude: Double, stopTypeId: Int, stopType: String, creationDate: String
    ) {
        this.taskId = taskId
        this.addedBy = addedBy
        this.StopName = stopName
        this.Latitude = latitude
        this.Longitude = longitude
        this.StopTypeID = stopTypeId
        this.StopType = stopType
        this.CreationDate = creationDate

    }
}