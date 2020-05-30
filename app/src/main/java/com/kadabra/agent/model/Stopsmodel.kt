package com.kadabra.agent.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Stopsmodel {

    @SerializedName("Longitude")
    @Expose
    var longitude: Double? = null
    @SerializedName("AddedBy")
    @Expose
    var addedBy: String? = null
    @SerializedName("Latitude")
    @Expose
    var latitude: Double? = null
    @SerializedName("StopName")
    @Expose
    var stopName: String? = null
    @SerializedName("StopTypeID")
    @Expose
    var stopTypeID: Int? = null
    @SerializedName("StopIndex")
    @Expose
    var stopIndex: Int = 0

    constructor(
        stopName: String,
        latitude: Double,
        longitude: Double,
        addedBy: String,
        stopTypeID: Int,
        stopIndex:Int
    ) {
        this.stopName = stopName
        this.latitude = latitude
        this.longitude = longitude
        this.addedBy = addedBy
        this.stopTypeID = stopTypeID
        this.stopIndex=stopIndex
    }

}