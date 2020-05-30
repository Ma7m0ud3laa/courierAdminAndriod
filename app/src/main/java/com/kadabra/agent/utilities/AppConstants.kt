package com.kadabra.agent.utilities

import android.location.Location
import android.net.Uri
import com.kadabra.agent.model.*


/**
 * Created by Mokhtar on 6/18/2019.
 */
object AppConstants {


    const val BASE_URL = "https://courier.kadabraservices.com/api/Account/"

    const val APP_DATABASE_NAME = "admin"
    // region BASaIC RESPONSE
    val STATUS = "status"
    val MESSAGE = "Message"
    val DATA = "data"
    val STATUS_SUCCESS = 1
    val STATUS_FAILED = -1
    val STATUS_NOT_EXIST = -2
    val STATUS_INCORRECT_DATA = -3

    //endregion
    // region CODE RESPONSE
    val CODE_204 = 204
    val CODE_201 = 201
    val CODE_200 = 1
    val CODE_1 = 200
    val CODE_444 = 444
    //endregion
    //region languages
    val LANGUAGE = "lang"
    val ARABIC = "ar"
    val ENGLISH = "en"
    val ALERT = "Alert"
    val ERROR = "Error"
    val INFO = "Info"
    val WARNING = "Warning"
    val OK = "ok"
    val CANCEL = "cancel"

    val IS_FIRST = "is_first"
    val IS_LOGIN = "login"
    val TICKET_SUB_DATA = "ticket_sub_data"
    val COURIERS_DATA = "couriers_data"

    var isMoving = false


    //endregion
    //region TOKEN
    var token = ""
    //endregion
    //region Auth

    const val URL_LOGIN = "AdminLogin"
    const val URL_LOG_OUT = "Logout"
    const val URL_ADD_TICKET = "AddTicket"
    const val URL_EDIT_TICKET = "EditTicket"
    const val URL_TICKET_SUB_DATA = "GetAllDataForAddingTicket"
    const val URL_ADD_TASK = "AddTask"
    const val URL_EDIT_TASK = "EditTask"
    const val URL_CANCEL_TASK = "cancelTask"
    const val URL_REASSIGN_TASK_TO_COURIER = "ReassignTaskToCourier"
    const val URL_GET_ALL_TICKETS = "GetAllTickets"
    const val URL_GET_ALL_TICKETS_BY_PAGE_NO = "GetAllTicketsByPageNo"
    const val URL_GET_ALL_TICKETS_SIMPLE = "GetAllTicketsSimpleData"
    const val URL_GET_ALL_TICKETS_BY_PAGE_NO_SIMPLE = "GetAllTicketsByPageNoSimpleData"


    const val URL_GET_TICKET_BY_ID = "GetTicketById"
    const val URL_GET_ALL_COURIERS = "GetAllCourier"
    const val URL_GET_ALL_COURIERS_WITH_STATUS = "GetAllCourierWithStatus  "

    const val URL_ADD_TASK_STOP = "AddTaskStop"
    const val URL_GET_ALL_TASK_STOPS = "GetAllTaskStops"
    const val URL_REMOVE_TASK = "RemoveCourierTask"
    const val URL_REMOVE_STOP = "RemoveTaskStop"
    const val URL_SET_USER_TOKEN = "UpdateAdminToken"
    const val URL_GET_VERSION_CODE = "GetVersionCodeForAndroidAdmin"
    const val URL_GET_TAKS_DETAILS = "GetTaskByID"
    const val URL_UPDATE_TASK_COURIER_FEES = "UpdateTaskCourierFees"
    const val URL_GET_ALL_COMPLETED_TASKS = "GetAllCompletedTaskForAdmin"
    const val URL_GET_ALL_NOTIFICATIONS = "GetAllAdminNotification"
    const val URL_GET_ALL_READED_NOTIFICATIONS = "UpdateAdminReadedNotification"
    const val URL_GET_CLIENT_NAME = "GetUserNameByMobile"
    const val URL_TAG_AGENT_IN_TICKET = "TagAgentInTicket"
    const val URL_END_TAKS = "UpdateEndTask"
    const val URL_GET_COURIER_TRESAURY = "GetCourierTreasuryById"

    //endregion

    //region  app variables
    const val TEST_MODE = "testMode"
    const val DEFAULT_LANGUAGE = 2
    const val TRUE = "true"
    const val FALSE = "false"

    var CurrentLoginAdmin: Admin = Admin()
    var CurrentSelectedTicket: Ticket = Ticket()
    var GetALLTicket: ArrayList<Ticket> = ArrayList()
    var CurrentCourierLocation: location = location()
    var CurrentSelectedTask: Task = Task()
    var CurrentSelectedStop: Stop = Stop()
    var CurrentTempStop: Stop = Stop()
    var ALL_COURIERS = ArrayList<Courier>()
    var ALL_COURIERS_FIREBASE = ArrayList<Courier>()
    var TICKET_TASK_LIST = ArrayList<Task>()
    var TICKET_SERVICE_COST_LIST = ArrayList<TicketServiceCost>()
    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002
    val ADMIN_CHANNEL_ID = "123456"
    var TASK_STOP_LIST = ArrayList<Stop>()
    var CurrentLocation: Location? = null
    var StopType: StopType? = null
    var CURRENT_IMAGE_URI: Uri? = null

    var FIRE_BASE_LOGOUT = false
    var FIRE_BASE_NEW_TASK = false
    var FIRE_BASE_EDIT_TASK = false
    var FIRE_BASE_DELETE_TASK = false
    var FIRE_BASE_REASSIGN_TASK = false

    var CurrentSelecedNotification = Notification()
    var COURIERSTARTTASK = false
    var CURRENTTOTALNOTIFICATIONS = 0
    var IN_PROGRESS = "In progress"
    var NEW = "New"
    var COMPLETED = "Completed"
    var WAITING = "Waiting For Pickup"
    var CANCELLED = "Cancelled"


    const val FIREBASE_HAVE_TASK = "haveTask"

    enum class TaskStatus(var status: String) {
        NEW("New"),
        IN_PROGRESS("In progress"),
        COMPLETED("Completed"),
        WAITING_FEEDBASK("Waiting feedback"),
        POST_PONDED("Postponed"),
        CANCELLED("Cancelled");
    }

    enum class TaskPriority(var status: String) {
        LOW("Low"),
        NORMAL("Normal"),
        HIGHT("High"),
        CRITICAL("Critical");
    }


    //endregion

}