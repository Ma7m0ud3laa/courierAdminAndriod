package com.twoam.agent.api


import com.twoam.agent.model.*
import com.twoam.agent.utilities.AppConstants
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*


/**
 * Created by Mokhtar on 1/5/2020.
 */
interface ApiServices {


    @GET(AppConstants.URL_LOGIN)
    fun logIn(@Query("userName") userName: String, @Query("password") password: String)
            : Call<ApiResponse<Admin>>


    @POST(AppConstants.URL_LOG_OUT)
    fun logOut(@Query("ID") id: Int)
            : Call<ApiResponse<Admin>>


    @GET(AppConstants.URL_GET_ALL_TICKETS)
    fun getAllTickets()
            : Call<ApiResponse<ArrayList<Ticket>>>

    @POST(AppConstants.URL_GET_TICKET_BY_ID)
    fun getTicketById(
        @Query("ticketId") ticketId: String
    )
            : Call<ApiResponse<Ticket>>


    @POST(AppConstants.URL_ADD_TASK)
    fun addTask(
        @Body tasModel: TaskModel
    )
            : Call<ApiResponse<ArrayList<Task>>>


    @POST(AppConstants.URL_EDIT_TASK)
    fun editTask(
        @Body tasModel: TaskModel
    )
            : Call<ApiResponse<ArrayList<Task>>>


    @POST(AppConstants.URL_REASSIGN_TASK_TO_COURIER)
    fun reAssignTaskToCourier(@Query("TaskId") taskId: String, @Query("CourierID") courierId: String)
            : Call<ApiResponse<Boolean>>

    @GET(AppConstants.URL_GET_ALL_COURIERS)
    fun getAllCouriers()
            : Call<ApiResponse<ArrayList<Courier>>>

    @FormUrlEncoded
    @POST(AppConstants.URL_ADD_TASK_STOP)
    fun addTaskStop(
        @Field("AddedBy") addedBy: String,
        @Field("TaskId") taskId: String,
        @Field("Latitude") latitude: String,
        @Field("Longitude") longitude: String,
        @Field("StopName") stopName: String,
        @Field("StopTypeID") sopTypeID: Int,
        @Field("StopType") stopType: String,
        @Field("CreationDate") creationDate: String

    )
            : Call<ApiResponse<ArrayList<Stop>>>


    @POST(AppConstants.URL_GET_ALL_TASK_STOPS)
    fun getAllTaskStops(@Query("TaskId") taskId: String)
            : Call<ApiResponse<ArrayList<Stop>>>


    @POST(AppConstants.URL_REMOVE_TASK)
    fun removeTask(
        @Query("TaskId") taskId: String,
        @Query("AdminId") adminId: String
    )
            : Call<ApiResponse<ArrayList<Task>>>


    @POST(AppConstants.URL_REMOVE_STOP)
    fun removeStop(
        @Query("stopId") stopId: String,
        @Query("AdminId") adminId: String
    )
            : Call<ApiResponse<ArrayList<Stop>>>


}

