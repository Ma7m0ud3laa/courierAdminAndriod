package com.twoam.agent.api


import com.twoam.agent.model.User
import com.twoam.agent.utilities.AppConstants
import retrofit2.Call
import retrofit2.http.*


/**
 * Created by Mokhtar on 1/5/2020.
 */
interface ApiServices {


    @POST (AppConstants.URL_LOGIN)
    fun logIn(@Query("email") email: String, @Query("password") password: String)
            : Call<ApiResponse<User>>

    @POST(AppConstants.URL_LOG_OUT)
    fun logOut(@Query("email") email: String, @Query("password") password: String)
            : Call<ApiResponse<User>>
}

