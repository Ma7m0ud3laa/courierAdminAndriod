package com.twoam.agent.model



/**
 * Created by Mokhtar on 1/8/2020.
 */
class DropOffLocation {

    var id: String = ""
    var name: String = ""
    var lat: Double? = null
    var lon: Double? = null

    constructor()
    constructor(id: String,name:String,lat: Double,lon: Double)
    {
        this.id=id
        this.name=name
        this.lat=lat
        this.lon=lon


    }
}