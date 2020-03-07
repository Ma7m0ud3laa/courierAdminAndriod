package com.kadabra.agent.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TicketModel {

    @SerializedName("TicketId")
    @Expose
    var ticketId: String = ""
    @SerializedName("TicketName")
    @Expose
    var ticketName: String = ""
    @SerializedName("TicketDescription")
    @Expose
    var ticketDescription: String = ""
    @SerializedName("UserMobile")
    @Expose
    var userMobile: String= ""
    @SerializedName("StatusId")
    @Expose
    var statusId: Int = 0
    @SerializedName("CategoryId")
    @Expose
    var categoryId: String = ""
    @SerializedName("PriorityId")
    @Expose
    var priorityId: Int = 0
    @SerializedName("PaymentId")
    @Expose
    var paymentId: Int = 0
    @SerializedName("NeedCourier")
    @Expose
    var needCourier: Boolean = false
    @SerializedName("ServiceCost")
    @Expose
    var serviceCosts: ArrayList<TicketServiceCost>? = null
    @SerializedName("AdminId")
    @Expose
    var adminId: String = ""

    constructor()
    constructor(ticketId: String,
                ticketName: String,
                ticketDescription: String,
                userMobile: String,
                categoryId: String,
                statusId: Int,
                priorityId: Int,
                paymentId: Int,
                needCourier:Boolean,
                serviceCosts: ArrayList<TicketServiceCost>,
                adminId: String

    )
    {
       this. ticketId=ticketId
        this.ticketName=ticketName
        this.ticketDescription=ticketDescription
        this.userMobile=userMobile
        this.statusId=statusId
        this.categoryId=categoryId
        this.priorityId=priorityId
        this.paymentId=paymentId
        this.needCourier=needCourier
        this.serviceCosts=serviceCosts
        this.adminId=adminId
    }

}
