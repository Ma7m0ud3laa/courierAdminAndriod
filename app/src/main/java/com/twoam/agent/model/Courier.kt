package com.twoam.agent.model

class Courier {
    var CourierId: Int = 0
    var CourierName: String = ""
    var CourierMobile: String = ""
    var courierLocation: Stop = Stop()


    constructor()
    constructor(id: Int, name: String) {
        this.CourierId = id
        this.CourierName = name
    }
}