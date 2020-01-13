package com.twoam.agent.model



/**
 * Created by Mokhtar on 1/8/2020.
 */
class Stop {

    var id: String = ""
    var name: String = ""
    var description: String = ""
    var pickUpLocation: PickUpLocation = PickUpLocation()

    constructor()
    constructor(id: String,name:String,description: String,pickUpLocation: PickUpLocation)
    {
        this.id=id
        this.name=name
        this.description=description
        this.pickUpLocation=pickUpLocation


    }

}