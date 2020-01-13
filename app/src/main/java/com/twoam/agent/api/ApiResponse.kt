package com.twoam.agent.api

/**
 * Created by Mokhtar on 6/19/2019.
 */
class ApiResponse<T> {

    var data: T?
    var success: Boolean = false
    var message: String = ""
    var code: Int = 0

    constructor(data: T?, code: Int, sucess: Boolean, messaage: String) {
        this.data = data
        this.success = success
        this.message = message
        this.code = code

    }
}