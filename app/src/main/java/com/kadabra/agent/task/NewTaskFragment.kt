package com.kadabra.agent.task


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.kadabra.Networking.INetworkCallBack
import com.kadabra.Networking.NetworkManager
import com.kadabra.agent.adapter.TicketListAdapter
import com.kadabra.agent.api.ApiResponse
import com.kadabra.agent.api.ApiServices
import com.kadabra.agent.callback.IBottomSheetCallback

import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.kadabra.agent.adapter.CourierListAdapter
import com.kadabra.agent.adapter.StopAdapter
import android.widget.ArrayAdapter
import com.kadabra.agent.R
import com.kadabra.agent.utilities.*
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kadabra.Utilities.Base.BaseFragment
import com.kadabra.agent.callback.ITaskCallback
import com.kadabra.agent.model.*
import com.kadabra.agent.utilities.Alert.hideProgress
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

import android.widget.TimePicker
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.app.DatePickerDialog

import android.util.Log
import com.google.android.libraries.places.internal.df
import com.google.android.libraries.places.internal.it
import java.text.DateFormat
import java.text.SimpleDateFormat


class NewTaskFragment : BaseFragment(), IBottomSheetCallback, ITaskCallback, View.OnClickListener {

    //region Members
    private var TAG = this.javaClass.simpleName
    private var ticketList = ArrayList<Ticket>()
    private var tasks = ArrayList<Task>()
    private var stopsList = ArrayList<Stop>()
    private var stopsModelList = ArrayList<Stopsmodel>()

    private var courierList = ArrayList<Courier>()
    private var dummyCourierList = ArrayList<Courier>()
    private var dummyTicketList = ArrayList<Ticket>()
    private var selectedCourier = Courier()
    private var selectedTicket = Ticket()
    private var selectedStopType: StopType? = null
    private var adapterTicket: TicketListAdapter? = null
    private var adapterCourier: CourierListAdapter? = null
    private var task = Task()
    private var taskModel = TaskModel()
    private var taskModelEdit = TaskModelEdit()

    var date: Calendar? = null
    var dateEdit: Calendar? = null

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0

    var editMode = false
    var taskAddMode = false

    private lateinit var currentView: View
    private lateinit var scroll: ScrollView
    private var ivBack: ImageView? = null
    private var tvTaskDetails: TextView? = null
    private var tvDeleteTask: TextView? = null
    private var listener: IBottomSheetCallback? = null
    private var listenerTask: ITaskCallback? = null
    private lateinit var etTaskName: EditText
    private lateinit var etTaskDescription: EditText

    private lateinit var etAmount: EditText
    private lateinit var etPickupTime: EditText

    private lateinit var sTicket: AutoCompleteTextView
    private lateinit var sCourier: AutoCompleteTextView
    private lateinit var tvAddStop: TextView
    private lateinit var tvClearStop: TextView
    private lateinit var tvGetLocation: TextView
    private lateinit var btnAddStopLocation: Button
    private lateinit var rlStops: RelativeLayout
    private lateinit var etStopName: EditText
    private lateinit var sStopType: Spinner
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var rvStops: RecyclerView
    private lateinit var btnSave: Button
    private lateinit var tvStatus: TextView
    private lateinit var ivDirection: ImageView

    private lateinit var refresh: SwipeRefreshLayout
    private var dateValue = ""
    //endregion

    //region Events
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(
            R.layout.fragment_new_task, container, false
        )
        init()
        getAllCouriers()
        prepareCourier(AppConstants.ALL_COURIERS)
        prepareStopType()
        sTicket.setText(AppConstants.CurrentSelectedTicket.TicketName)
        sTicket.isEnabled = false

        if (editMode) {
            btnSave.text = context!!.getString(com.kadabra.agent.R.string.update)
            tvTaskDetails!!.text = context!!.getString(com.kadabra.agent.R.string.task_details)
            tvDeleteTask!!.visibility = View.VISIBLE
            getTaskDetails(AppConstants.CurrentSelectedTask.TaskId)
        } else
            defaultTaskData()

        return currentView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        }
        if (context is ITaskCallback) {
            listenerTask = context
        } else {
            throw ClassCastException("$context must implement IBottomSheetCallback.onBottomSheetSelectedItem")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        listenerTask = null
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBottomSheetSelectedItem(index: Int) {
        listener!!.onBottomSheetSelectedItem(3)
    }

    override fun onTaskDelete(task: Task?) {

    }

    override fun onStopDelete(stop: Stop?) {

        if (AppConstants.CurrentSelectedTask.Status == AppConstants.NEW) {
            if (!stop!!.StopID.isNullOrEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle(AppConstants.WARNING)
                    .setMessage(getString(com.kadabra.agent.R.string.message_delete) + " " + stop.StopName + " ?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(AppConstants.OK) { _, _ ->
                        removeStop(stop)
                    }
                    .setNegativeButton(AppConstants.CANCEL) { _, _ -> }
                    .show()

            } else {
                stopsList.remove(stop)
                loadTaskStops(stopsList)
            }
        } else {
            Alert.showAlertMessage(
                context!!,
                AppConstants.WARNING,
                "Can't delete this Stop this task is In progress."
            )
        }


    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //todo set the selected stop data
            var stop = AppConstants.CurrentTempStop
            if (!stop.Latitude.toString().isNullOrEmpty()) {
                etStopName.setText(stop.StopName)
                sStopType.setSelection(0)
                sStopType.setSelection(0)
                etLatitude.setText(stop.Latitude.toString())
                etLongitude.setText(stop.Longitude.toString())
            }

        } else {
//            stopsList.clear()
//            stopsModelList.clear()
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.ivBack -> {
                AppConstants.CurrentSelectedTask = Task()  //reset selected task
                listener!!.onBottomSheetSelectedItem(3)  //back to ticket details
            }
            R.id.tvDeleteTask -> {

                // if (!AppConstants.CurrentSelectedTask.TaskId.isNullOrEmpty() && AppConstants.CurrentSelectedTask.Status != AppConstants.IN_PROGRESS) {
                if (!AppConstants.CurrentSelectedTask.TaskId.isNullOrEmpty() && AppConstants.CurrentSelectedTask.Status == AppConstants.NEW) {
                    var task = AppConstants.CurrentSelectedTask
                    AlertDialog.Builder(context)
                        .setTitle(AppConstants.WARNING)
                        .setMessage(getString(com.kadabra.agent.R.string.message_delete) + " " + task.TaskName + " ?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(AppConstants.OK) { dialog, which ->
                            deleteTask(task)
                        }
                        .setNegativeButton(AppConstants.CANCEL) { dialog, which -> }
                        .show()

                } else
                    Alert.showAlertMessage(
                        context!!,
                        AppConstants.WARNING,
                        "Can't delete this task."
                    )
            }
            R.id.tvAddStop -> {
                if (AppConstants.CurrentSelectedTask.Status != AppConstants.IN_PROGRESS &&
                    AppConstants.CurrentSelectedTask.Status != AppConstants.COMPLETED) {
                    if (!rlStops.isVisible)
                        rlStops.visibility = View.VISIBLE
                } else
                    Alert.showAlertMessage(
                        context!!,
                        AppConstants.WARNING,
                        "Can't edit this task."
                    )
            }


            R.id.tvClearStop -> {
                if (rlStops.isVisible) {
                    rlStops.visibility = View.GONE

                }

            }

            R.id.tvGetLocation -> {
                if (AppConstants.CurrentSelectedTask.TaskId.trim().isNullOrEmpty()
                    ||AppConstants.CurrentSelectedTask.Status== AppConstants.NEW) {
                    Log.d(TAG,AppConstants.CurrentSelectedTask.Status)
                    hideKeyboard(tvGetLocation)
                    //to do open map and get selected location
                    listener!!.onBottomSheetSelectedItem(8)
                } else
                    Alert.showAlertMessage(
                        context!!,
                        AppConstants.WARNING,
                        "Can't edit this task."
                    )
            }
            R.id.etPickupTime -> {
                showDateTimePicker()
            }

            R.id.btnAddStopLocation -> {
                if (AppConstants.CurrentSelectedTask.TaskId.trim().isNullOrEmpty()
                    ||AppConstants.CurrentSelectedTask.Status== AppConstants.NEW) {
                    context!!.getSystemService(Context.INPUT_METHOD_SERVICE)
                    if (validateStopData()) {
                        hideKeyboard(btnAddStopLocation)
                        etStopName.requestFocus()

                        var stopName = etStopName.text.toString()
                        var stopTypeId = sStopType.selectedItemPosition + 1
                        var stopType = sStopType.selectedItem.toString()
                        var latitude = etLatitude.text.toString().toDouble()
                        var longitude = etLongitude.text.toString().toDouble()


                        var stop = Stop(
                            AppConstants.CurrentSelectedTask.TaskId,
                            AppConstants.CurrentLoginAdmin.AdminId,
                            stopName,
                            latitude,
                            longitude,
                            stopTypeId,
                            stopType,
                            "", 0 //new
                        )

                        if (validateStopType(stop)) {
                            addStopToStopList(stop)
                        } else
                            Alert.showMessage(
                                context!!,
                                "The task must include at least one Pickup and one Drop Off Stops."
                            )


                    }
                } else
                    Alert.showAlertMessage(
                        context!!,
                        AppConstants.WARNING,
                        "Can't edit this task."
                    )
                Log.d(TAG, "New")

            }


            R.id.btnSave -> {
                //                if (AppConstants.CurrentLoginAdmin.IsSuperAdmin) { //
//                if (AppConstants.CurrentSelectedTask.Status == AppConstants.NEW) {
                    if (validateAll()) {
                        hideKeyboard(btnSave)
                        prepareTaskData()
                        if (!editMode) {
                            addTask(taskModel)
                        }
//                        else if (editMode && AppConstants.CurrentSelectedTask.Status != AppConstants.IN_PROGRESS)
                        else if (editMode && AppConstants.CurrentSelectedTask.Status == AppConstants.NEW)
                            editTask(taskModelEdit)
                        else
                            Alert.showMessage(
                                context!!,
                                "Can't edit this task."
                            )
                    }
//                } else
//                    Alert.showAlertMessage(
//                        context!!,
//                        AppConstants.WARNING,
//                        "Can't edit this task."
//                    )

            }
            R.id.ivDirection -> //get current courier direction if task is accepted
            {
//                if (AppConstants.CurrentSelectedTask.Status == AppConstants.IN_PROGRESS) {
                if (AppConstants.CurrentSelectedTask.Status == AppConstants.IN_PROGRESS)
                    listener?.onBottomSheetSelectedItem(17)//navigate to courier map
else
                    Alert.showMessage(context!!,"This task not started yet.")
//                }


            }


        }
    }


//endregion


    //region Helper Functions
    private fun init() {
        scroll = currentView.findViewById(com.kadabra.agent.R.id.scroll)
        refresh = currentView!!.findViewById(com.kadabra.agent.R.id.refresh)
        ivBack = currentView!!.findViewById(com.kadabra.agent.R.id.ivBack)
        tvTaskDetails = currentView!!.findViewById(com.kadabra.agent.R.id.tvTaskDetails)
        tvDeleteTask = currentView!!.findViewById(com.kadabra.agent.R.id.tvDeleteTask)
        etTaskName = currentView!!.findViewById(com.kadabra.agent.R.id.etTaskName)
        etTaskDescription = currentView!!.findViewById(com.kadabra.agent.R.id.etTaskDescription)
        tvStatus = currentView!!.findViewById(com.kadabra.agent.R.id.tvStatus)
        ivDirection = currentView!!.findViewById(com.kadabra.agent.R.id.ivDirection)

        sTicket = currentView!!.findViewById(com.kadabra.agent.R.id.sTicket)
        sCourier = currentView!!.findViewById(com.kadabra.agent.R.id.sCourier)
        etAmount = currentView!!.findViewById(com.kadabra.agent.R.id.etAmount)
        etPickupTime = currentView!!.findViewById(com.kadabra.agent.R.id.etPickupTime)

        tvAddStop = currentView!!.findViewById(com.kadabra.agent.R.id.tvAddStop)
        tvClearStop = currentView!!.findViewById(com.kadabra.agent.R.id.tvClearStop)
        tvGetLocation = currentView!!.findViewById(com.kadabra.agent.R.id.tvGetLocation)

        rlStops = currentView!!.findViewById(com.kadabra.agent.R.id.rlStops)
        etStopName = currentView!!.findViewById(com.kadabra.agent.R.id.etStopName)
        sStopType = currentView!!.findViewById(com.kadabra.agent.R.id.sStopType)
        etLatitude = currentView!!.findViewById(com.kadabra.agent.R.id.etLatitude)
        etLongitude = currentView!!.findViewById(com.kadabra.agent.R.id.etLongitude)
        btnAddStopLocation = currentView!!.findViewById(com.kadabra.agent.R.id.btnAddStopLocation)
        rvStops = currentView!!.findViewById(com.kadabra.agent.R.id.rvStops)
        btnSave = currentView!!.findViewById(com.kadabra.agent.R.id.btnSave)



        ivBack!!.setOnClickListener(this)
        tvDeleteTask!!.setOnClickListener(this)
        tvAddStop!!.setOnClickListener(this)
        tvClearStop!!.setOnClickListener(this)
        tvGetLocation!!.setOnClickListener(this)
        etPickupTime.setOnClickListener(this)
        ivDirection.setOnClickListener(this)

        btnAddStopLocation!!.setOnClickListener(this)
        btnSave!!.setOnClickListener(this)



        refresh.setOnRefreshListener {
            if (editMode)
                getTaskDetails(AppConstants.CurrentSelectedTask.TaskId)
            else
                refresh.isRefreshing = false
        }

    }

    private fun defaultTaskData() {


        btnSave.text = context!!.getString(com.kadabra.agent.R.string.save)
        tvTaskDetails!!.hint = context!!.getString(com.kadabra.agent.R.string.new_task)

        tvDeleteTask!!.visibility = View.INVISIBLE

        etTaskName.hint = context!!.getString(R.string.task_name)
        etTaskDescription.hint = context!!.getString(R.string.task_description)
        tvStatus.text = getString(R.string.new_status)

//        sCourier.setText(context!!.getString(R.string.select_courier))
        etAmount!!.hint = context!!.getString(R.string.amount)

        etPickupTime!!.hint = context!!.getString(R.string.pickup_time)



        rvStops.adapter = null
        rlStops.visibility = View.GONE

        taskAddMode = false
    }

    private fun loadTaskData(task: Task) {


        selectedTicket =
            AppConstants.GetALLTicket.find { it.TicketId == AppConstants.CurrentSelectedTicket.TicketId }!!


        if (!task.TaskName.trim().isNullOrEmpty())
            etTaskName.setText(task.TaskName)


        tvStatus.text = task.Status

        if (task.TaskDescription != null && !task.TaskDescription.isNullOrEmpty())
            etTaskDescription.setText(task.TaskDescription)

        if (task.CourierID != null) {
            sCourier.setText(task.CourierName)
            selectedCourier = Courier(task.CourierID, task.CourierName)
        }

        if (!task.Amount.toString().trim().isNullOrEmpty())
            etAmount!!.setText(task.Amount.toString())


        if (!task.PickUpTime.trim().isNullOrEmpty()) {

            var t = "yyyy-MM-dd'T'HH:mm:ss"

            var myFormat = "EEE, MMM d, yyyy   hh:mm a" //

            var sdf = SimpleDateFormat(t, Locale.US)
            var tf = SimpleDateFormat(myFormat, Locale.US)
            var dateObj = sdf.parse(task.PickUpTime)
            dateEdit = Calendar.getInstance()
            dateEdit?.time = dateObj

            etPickupTime?.setText(tf.format(dateObj!!.time))


        }



        if (task.stopsmodel.count() > 0) {

            rlStops.visibility = View.VISIBLE

            stopsList = task.stopsmodel


            loadTaskStops(task.stopsmodel)

        }

//        prepareTaskModelData(AppConstants.CurrentSelectedTask)
    }

    private fun prepareTaskModelData(task: Task) {

        if (editMode == false) {
            taskModel.taskName = task.TaskName
            taskModel.TaskDescription = task.TaskDescription
            taskModel.amount = task.Amount
            taskModel.ticketID = task.TicketId
            taskModel.courierId = task.CourierID
            taskModel.pickupTime = task.PickUpTime

            taskModel.addedBy = task.AddedBy

            taskModel.stopsmodels.clear()
            task.stopsmodel.forEach {


                taskModel.stopsmodels!!.add(
                    Stopsmodel(
                        it.StopName,
                        it.Latitude!!,
                        it.Longitude!!,
                        task.AddedBy,
                        it.StopTypeID
                    )
                )
//            }
            }

        } else {


            taskModelEdit.taskId = task.TaskId
            taskModelEdit.modifiedBy = task.AddedBy

            taskModelEdit.taskName = task.TaskName
            taskModelEdit.TaskDescription = task.TaskDescription
            taskModelEdit.amount = task.Amount
            taskModelEdit.ticketID = task.TicketId
            taskModelEdit.courierId = task.CourierID
            taskModelEdit.pickupTime = task.PickUpTime

            taskModelEdit.modifiedBy = task.AddedBy

            taskModelEdit.stopsmodels.clear()
            task.stopsmodel.forEach {


                taskModelEdit.stopsmodels!!.add(
                    Stopsmodel(
                        it.StopName,
                        it.Latitude!!,
                        it.Longitude!!,
                        task.AddedBy,
                        it.StopTypeID
                    )
                )
//            }
            }

        }

    }

    private fun validateAll(): Boolean {

        var pick = stopsList.count { it.StopTypeID == 1 }
        var drop = stopsList.count { it.StopTypeID == 2 }

        if (etTaskName.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "TaskName Name is required.")
            AnimateScroll.scrollToView(scroll, etTaskName)
            etTaskName.requestFocus()
            return false
        } else if (etTaskDescription.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "TaskName Description is required.")
            AnimateScroll.scrollToView(scroll, etTaskDescription)
            etTaskDescription.requestFocus()
            return false
        }
//        else if (etAmount.text.toString().isNullOrEmpty()) {
//            Alert.showMessage(context!!, "Amount is required.")
//            AnimateScroll.scrollToView(scroll, etAmount)
//            etAmount.requestFocus()
//            return false
//        }
        else if (etPickupTime.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Pickup Time is required.")
            AnimateScroll.scrollToView(scroll, etPickupTime)
            etPickupTime.requestFocus()
            return false
        } else if (selectedCourier.CourierId == null) {
            Alert.showMessage(context!!, "Courier is required.")
            AnimateScroll.scrollToView(scroll, sCourier)
            sCourier.showDropDown()
            return false
        } else if (rlStops.isVisible && stopsList.size <= 0 &&
            etLatitude.text.toString().isNotEmpty() || etLongitude.text.toString().isNotEmpty()
        ) {

            Alert.showMessage(context!!, "Complete add Stop data.")
            AnimateScroll.scrollToView(scroll, etLongitude)
            btnAddStopLocation.requestFocus()
            return false
        } else if (stopsList.size < 2) {
            Alert.showMessage(context!!, "Cant save task without Pickup and Drop off stops.")
            rlStops.visibility = View.VISIBLE
            AnimateScroll.scrollToView(scroll, etStopName)
            etStopName.requestFocus()

            return false
        } else if (stopsList.size >= 2 && pick == 0 || drop == 0) {
            AnimateScroll.scrollToView(scroll, etStopName)
            etStopName.requestFocus()
            if (pick < 1)
                Alert.showMessage(
                    context!!,
                    "The task must include one Pickup Stop ."
                )
            if (drop < 1)
                Alert.showMessage(
                    context!!,
                    "The task must include one Drop Off Stop ."
                )


            return false
        }

//        else if (stopsList.size >= 2) {
//            var pick = stopsList.count { it.StopTypeID == 1 }
//            var drop = stopsList.count { it.StopTypeID == 2 }
//
//             if (pick == 1 || drop == 1) {
//                Alert.showMessage(
//                    context!!, "The task must include at least one Pickup and one Drop Off Stops.")
//
//                return false
//
//            }
//            return true
//
//        }

        return true

    }

    private fun prepareTaskData() {
        var taskName = etTaskName.text.toString()
        var taskDescription = etTaskDescription.text.toString()

        var amount = 0.0//etAmount.text.toString().toDouble()

        if (dateValue.isNullOrEmpty())
            dateValue = AppConstants.CurrentSelectedTask.PickUpTime

        var pickupTime = dateValue

        var addedBY = AppConstants.CurrentLoginAdmin.AdminId
        var ticketId = AppConstants.CurrentSelectedTicket.TicketId
        var taskId = AppConstants.CurrentSelectedTask.TaskId
        var courierId = selectedCourier.CourierId

        task = Task(
            taskName,
            taskDescription,
            amount,
            pickupTime,
            addedBY,
            ticketId!!,
            taskId,
            courierId!!,
            stopsList
        )
        prepareTaskModelData(task)

    }


    private fun addTask(taskData: TaskModel) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.addTask(
                taskData
            )
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Task>>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Task>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
                            var tasks = response.ResponseObj!!
                            var task = tasks[0]
                            // add new task to the current ticket
                            AppConstants.CurrentSelectedTicket.taskModel.add(task)
                            listener!!.onBottomSheetSelectedItem(3)

                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }
    }


    private fun editTask(taskModel: TaskModelEdit) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.editTask(
                taskModel
            )
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Task>>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Task>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {


                            var tasks = response.ResponseObj!!
                            var task = tasks[0]
                            var selectedTaskIndex =
                                AppConstants.CurrentSelectedTicket.taskModel.indexOf(AppConstants.CurrentSelectedTicket.taskModel.find { it.TaskId == task.TaskId })
                            AppConstants.CurrentSelectedTicket.taskModel[selectedTaskIndex] = task
                            AppConstants.CurrentSelectedTask = task
                            Alert.hideProgress()
                            listener!!.onBottomSheetSelectedItem(3)
                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }
    }


    private fun addStop(stop: Stop) {

        var request = NetworkManager().create(ApiServices::class.java)
        var endPoint = request.addTaskStop(
            task.AddedBy,
            task.TaskId,
            stop.Latitude.toString(),
            stop.Longitude.toString(),
            stop.StopName,
            stop.StopTypeID,
            stop.StopType,
            ""
        )
        NetworkManager().request(
            endPoint,
            object : INetworkCallBack<ApiResponse<ArrayList<Stop>>> {
                override fun onFailed(error: String) {
                    Alert.showMessage(
                        context!!,
                        getString(R.string.error_login_server_error)
                    )
                }

                override fun onSuccess(response: ApiResponse<ArrayList<Stop>>) {
                    if (response.Status == AppConstants.STATUS_SUCCESS) {
                        stopsList = response.ResponseObj!!

                    } else if (response.Status == AppConstants.STATUS_FAILED) {
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                }
            })


    }

    private fun removeStop(stop: Stop) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.removeStop(
                stop.StopID, AppConstants.CurrentLoginAdmin.AdminId
            )
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Stop>>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Stop>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
                            stopsList = response.ResponseObj!!
                            AppConstants.CurrentSelectedTask.stopsmodel = stopsList
                            loadTaskStops(stopsList)

                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }
    }

    private fun getAllTaskStops(task: Task) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getAllTaskStops(task.TaskId)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Stop>>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Stop>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
                            stopsList = response.ResponseObj!!
                            loadTaskStops(stopsList)

                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }
    }

    private fun getAllCouriers() {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getAllCouriers()
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Courier>>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Courier>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
                            courierList = response.ResponseObj!!
                            AppConstants.ALL_COURIERS = courierList
                            prepareCourier(courierList)

                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }
    }

    private fun reAssignTaskToCourier(taskId: String, courierId: String) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.reAssignTaskToCourier(taskId, courierId)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<Boolean>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Boolean>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
                            var done = response.ResponseObj!!

                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }
    }

    private fun prepareTickets() {

        ticketList = AppConstants.GetALLTicket
        dummyTicketList.clear()
        if (ticketList.size > 0) {

            var newArray = ticketList

            var firstItem = Ticket("0", getString(R.string.select_ticket))
            dummyTicketList.add(firstItem)

            for (c in newArray.indices) {
                dummyTicketList.add(newArray[c])
            }
        }

        adapterTicket =
            TicketListAdapter(
                context!!,
                android.R.layout.simple_spinner_dropdown_item,
                dummyTicketList
            )
        sTicket.setAdapter(adapterTicket)
        sTicket.isCursorVisible = false


        sTicket.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                sTicket.showDropDown()
                var ticket = parent.getItemAtPosition(position) as Ticket


                if (ticket.TicketId != "0") {
                    selectedTicket = ticket
                    sTicket.setText(ticket.TicketName)
                    AppConstants.CurrentSelectedTicket = ticket
                } else {
                    selectedTicket = Ticket("0", getString(R.string.select_ticket))
                    sTicket.setText(getString(R.string.select_ticket))
                }
            }

        sTicket.setOnClickListener(View.OnClickListener {
            sTicket.showDropDown()

        })

//        // set current ticket
//        sTicket.setText(AppConstants.CurrentSelectedTicket.TicketName)

    }

    private fun prepareCourier(courierList: ArrayList<Courier>) {
        dummyCourierList.clear()
        if (courierList.size > 0) {
            var newArray = courierList

            var firstItem = Courier(0, getString(R.string.select_courier))
            dummyCourierList.add(firstItem)

            for (c in newArray.indices) {
                dummyCourierList.add(newArray[c])
            }
        }


        adapterCourier = CourierListAdapter(
            context!!,
            android.R.layout.simple_spinner_dropdown_item,
            dummyCourierList
        )
        sCourier.setAdapter(adapterCourier)
        sCourier.isCursorVisible = false

        sCourier.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                sCourier.showDropDown()
                var courier = parent.getItemAtPosition(position) as Courier


                if (courier.CourierId!! > 0) {
                    selectedCourier = courier
                    sCourier.setText(courier.CourierName)
                } else {
                    selectedCourier = Courier(0, getString(R.string.select_courier))
                    sCourier.setText(getString(R.string.select_courier))
                }


            }

        sCourier.setOnClickListener(View.OnClickListener {
            sCourier.showDropDown()

        })


    }


    private fun loadTaskStops(stopList: ArrayList<Stop>) {
        stopList.forEach { it.status = 1/*1 == update*/ }
        var adapter = StopAdapter(context!!, stopList, this)
        rvStops.adapter = adapter
        rvStops.layoutManager =
            LinearLayoutManager(AppController.getContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter.notifyDataSetChanged()


    }

    private fun prepareStopType() {
        sStopType.adapter = ArrayAdapter<StopType>(
            context!!,
            android.R.layout.simple_spinner_dropdown_item,
            StopType.values()
        )


        sStopType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {

                selectedStopType = parent.selectedItem as StopType

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    private fun saveStopsToTask(stopList: ArrayList<Stop>, task: Task) {
        var counter = 0
        if (stopsList.size > 0) {
            if (NetworkManager().isNetworkAvailable(context!!)) {
                stopList.forEach {
                    when (it.status) {
                        0 -> // new add
                        {
                            addStop(it)
                            task.stopsmodel.add(it)
                            counter += 1
                        }
                        1 -> // temp stop remove from list only
                        {

                        }
                        2 -> // remove
                        {
                            removeStop(it)
                            task.stopsmodel.remove(it)
                            counter += 1
                        }
                    }
                }


            } else {
                Alert.showMessage(context!!, getString(R.string.no_internet))
            }


        }
        listener!!.onBottomSheetSelectedItem(3)

    }

    private fun addStopToStopList(stop: Stop) {
        stopsList.add(stop)


        var adapter = StopAdapter(context!!, stopsList, this)
        rvStops.adapter = adapter
        rvStops.layoutManager =
            LinearLayoutManager(AppController.getContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter.notifyDataSetChanged()
        Alert.showMessage(context!!, "Stop ${stop.StopName} is added successfully.")
        clearStopControlsData()

    }


    private fun clearStopControlsData() {

        sStopType.setSelection(0)
        etStopName.setText("")
        etStopName.hint = context!!.getString(R.string.stop_name)
        etLatitude.setText("")
        etLatitude.hint = context!!.getString(R.string.latitude)
        etLongitude.setText("")
        etLongitude.hint = context!!.getString(R.string.longitude)
    }

    private fun validateStopData(): Boolean {
        if (etStopName.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Stop Name is required.")
            AnimateScroll.scrollToView(scroll, etStopName)
            etStopName.requestFocus()
            return false
        } else if (etLatitude.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Latitude is required.")
            AnimateScroll.scrollToView(scroll, etLatitude)
            etLatitude.requestFocus()
            return false
        } else if (etLongitude.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Longitude is required.")
            AnimateScroll.scrollToView(scroll, etLongitude)
            etLongitude.requestFocus()
            return false
        }

        return true
    }

    private fun validateStopType(stop: Stop): Boolean {

        var pick = stopsList.count { it.StopTypeID == 1 }
        var drop = stopsList.count { it.StopTypeID == 2 }

        if (pick == 1 && stop.StopTypeID == 1 || drop == 1 && stop.StopTypeID == 2)
            return false



        return true


    }


    private fun deleteTask(task: Task) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.removeTask(task.TaskId, AppConstants.CurrentLoginAdmin.AdminId)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<Boolean?>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            context!!.getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Boolean?>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
//                            var tasks = response.ResponseObj!!
//                            AppConstants.CurrentSelectedTicket.taskModel = tasks
//                            //  close and go to task details view
                            editMode = false
                            listener!!.onBottomSheetSelectedItem(3)

                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                context!!.getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                context!!.getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(context!!, context!!.getString(R.string.no_internet))
        }
    }

    fun convertTaskDataToJson(task: Task): JSONObject {

        val jResult = JSONObject()
        val jArray = JSONArray()

        val array = arrayOfNulls<Stop>(task.stopsmodel.size)
        for (i in task.stopsmodel.indices) {
            array[i] = task.stopsmodel[i]
        }

        jResult.putOpt("TaskName", task.TaskName)
        jResult.putOpt("Amount", task.Amount.toString())
        jResult.putOpt("AddedBy", task.AddedBy)
        jResult.putOpt("TicketID", task.TicketId)
        jResult.putOpt("CourierId", task.CourierID.toString())


        for (i in 0 until array.count()) {
            val jGroup = JSONObject()
            jGroup.put("Longitude", array[i]?.Longitude.toString())
            jGroup.put("AddedBy", task.AddedBy)
            jGroup.put("Latitude", array[i]?.Latitude.toString())
            jGroup.put("StopName", array[i]?.StopName.toString())
            jGroup.put("StopTypeID", array[i]?.StopTypeID.toString())


            jArray.put(jGroup)
        }

        jResult.putOpt("stopsmodels", jArray)

        return jResult
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager =
            context!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager!!.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun getTaskDetails(taskId: String) {
        refresh.isRefreshing = false
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getTaskDetails(taskId)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<Task>> {
                    override fun onFailed(error: String) {
                        hideProgress()
                        refresh.isRefreshing = false
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Task>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {

                            task = response.ResponseObj!!
                            AppConstants.CurrentSelectedTask = task
                            loadTaskData(task)
                            hideProgress()
                            refresh.isRefreshing = false
                        } else {
                            hideProgress()
                            refresh.isRefreshing = false
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_network)
                            )
                        }

                    }
                })

        } else {
            hideProgress()
            refresh.isRefreshing = false
            Alert.showMessage(
                context!!,
                getString(R.string.no_internet)
            )
        }


    }

    fun showDateTimePicker() {

        if (editMode) {

            val currentDate = Calendar.getInstance()
            date = Calendar.getInstance()

            mMonth = dateEdit!!.get(Calendar.MONTH)
            mDay = dateEdit!!.get(Calendar.DATE)
            mYear = dateEdit!!.get(Calendar.YEAR)
            mHour = dateEdit!!.get(Calendar.HOUR_OF_DAY)
            mMinute = dateEdit!!.get(Calendar.MINUTE)


            date!!.set(mYear, mMonth, mDay)

            DatePickerDialog(
                context,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    date!!.set(year, monthOfYear, dayOfMonth)
                    TimePickerDialog(
                        context,
                        TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                            date!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            date!!.set(Calendar.MINUTE, minute)
                            Log.d(TAG, "The choosen one ." + date!!.time)

                            var df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss") //.SSSXXX

                            var tf = SimpleDateFormat("EEE, MMM d, yyyy  hh:mm a")

                            dateValue = df.format(date!!.time)
                            var displayText = tf.format(date!!.time)

                            etPickupTime.setText(displayText)

                        },
                        currentDate.get(Calendar.HOUR_OF_DAY),
                        currentDate.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DATE)
            ).show()

        } else {
            val currentDate = Calendar.getInstance()
            date = Calendar.getInstance()


            DatePickerDialog(
                context,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    date!!.set(year, monthOfYear, dayOfMonth)
                    TimePickerDialog(
                        context,
                        TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                            date!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            date!!.set(Calendar.MINUTE, minute)
                            Log.d(TAG, "The choosen one ." + date!!.time)

                            var df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss") //.SSSXXX

                            var tf = SimpleDateFormat("EEE, MMM d, yyyy  hh:mm a")

                            dateValue = df.format(date!!.time)
                            var displayText = tf.format(date!!.time)

                            etPickupTime.setText(displayText)

                        },
                        currentDate.get(Calendar.HOUR_OF_DAY),
                        currentDate.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DATE)
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Alert.hideProgress()
    }

//endregion


}
