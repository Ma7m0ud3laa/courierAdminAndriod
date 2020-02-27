package com.twoam.agent.model

class Courier {
    var CourierId: Int? = null
    var name: String = ""
    var CourierMobile: String = ""
    var courierLocation: Stop = Stop()
    var isActive=false
    var location=location()
    var locations=HashMap<String,location>()
    var token: String = ""



    constructor()
    constructor(id: Int?, name: String) {
        this.CourierId = id
        this.name = name
    }
}