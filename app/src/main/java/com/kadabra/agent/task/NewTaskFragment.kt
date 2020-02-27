package com.twoam.agent.task


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.twoam.Networking.INetworkCallBack
import com.twoam.Networking.NetworkManager
import com.twoam.agent.adapter.TicketListAdapter
import com.twoam.agent.api.ApiResponse
import com.twoam.agent.api.ApiServices
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.cartello.Utilities.Base.BaseFragment
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoam.agent.adapter.CourierListAdapter
import com.twoam.agent.adapter.StopAdapter
import android.widget.ArrayAdapter
import com.twoam.agent.R
import com.twoam.agent.utilities.*
import android.widget.Spinner
import androidx.core.view.isVisible
import com.twoam.agent.callback.ITaskCallback
import com.twoam.agent.model.*
import org.json.JSONArray
import org.json.JSONObject


class NewTaskFragment : BaseFragment(), IBottomSheetCallback, ITaskCallback, View.OnClickListener {

    //region Members
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
    private lateinit var etAmount: EditText
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
        return currentView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
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
        if (!stop!!.StopID.isNullOrEmpty()) {
            AlertDialog.Builder(context)
                .setTitle(AppConstants.WARNING)
                .setMessage(getString(R.string.message_delete) + " " + stop.StopName + " ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(AppConstants.OK) { _, _ ->
                    removeStop(stop)
                }
                .setNegativeButton(AppConstants.CANCEL) { _, _ -> }
                .show()

        } else
            stopsList.remove(stop)
        var stopModel = stopsModelList.find { it.stopName == stop.StopName }
        stopsModelList.remove(stopModel)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //todo set the selected stop data
            var stop = AppConstants.CurrentTempStop
            etStopName.setText(stop.StopName)
            sStopType.setSelection(0)
            sStopType.setSelection(0)
            etLatitude.setText(stop.Latitude.toString())
            etLongitude.setText(stop.Longitude.toString())
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.ivBack -> {
                if (editMode)
                    listener!!.onBottomSheetSelectedItem(3)
                else
                    listener!!.onBottomSheetSelectedItem(0)

            }
            R.id.tvDeleteTask -> {
                if (!AppConstants.CurrentSelectedTask.TaskId.isNullOrEmpty()) {
                    var task = AppConstants.CurrentSelectedTask
                    AlertDialog.Builder(context)
                        .setTitle(AppConstants.WARNING)
                        .setMessage(getString(R.string.message_delete) + " " + task.Task + " ?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(AppConstants.OK) { dialog, which ->
                            deleteTask(task)
                        }
                        .setNegativeButton(AppConstants.CANCEL) { dialog, which -> }
                        .show()

                }
            }
            R.id.tvAddStop -> {
                if (!rlStops.isVisible)
                    rlStops.visibility = View.VISIBLE

            }

            R.id.tvClearStop -> {
                if (rlStops.isVisible) {
                    // todo  empty stop list
                    rlStops.visibility = View.GONE

//                    stopsList.clear()
//                    stopsModelList.clear()
//                    rvStops.adapter = null

                }

            }

            R.id.tvGetLocation -> {
                //to do open map and get selected location
                listener!!.onBottomSheetSelectedItem(8)
            }

            R.id.btnAddStopLocation -> {

                context!!.getSystemService(Context.INPUT_METHOD_SERVICE)
                if (validateStopData()) {
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

                    addStopToStopList(stop)

                }

            }


            R.id.btnSave -> {
                if (validateAll()) {
                    prepareTaskData()
                    if (!editMode) {
                        addTask(taskModel)
                    } else {
                        editTask(taskModel)
                    }
                }

            }
        }
    }

    //endregion


    //region Helper Functions
    private fun init() {
        scroll = currentView.findViewById(R.id.scroll)
        ivBack = currentView!!.findViewById(R.id.ivBack)
        tvTaskDetails = currentView!!.findViewById(R.id.tvTaskDetails)
        tvDeleteTask = currentView!!.findViewById(R.id.tvDeleteTask)
        etTaskName = currentView!!.findViewById(R.id.etTaskName)
        sTicket = currentView!!.findViewById(R.id.sTicket)
        sCourier = currentView!!.findViewById(R.id.sCourier)
        etAmount = currentView!!.findViewById(R.id.etAmount)
        tvAddStop = currentView!!.findViewById(R.id.tvAddStop)
        tvClearStop = currentView!!.findViewById(R.id.tvClearStop)
        tvGetLocation = currentView!!.findViewById(R.id.tvGetLocation)

        rlStops = currentView!!.findViewById(R.id.rlStops)
        etStopName = currentView!!.findViewById(R.id.etStopName)
        sStopType = currentView!!.findViewById(R.id.sStopType)
        etLatitude = currentView!!.findViewById(R.id.etLatitude)
        etLongitude = currentView!!.findViewById(R.id.etLongitude)
        btnAddStopLocation = currentView!!.findViewById(R.id.btnAddStopLocation)
        rvStops = currentView!!.findViewById(R.id.rvStops)
        btnSave = currentView!!.findViewById(R.id.btnSave)



        ivBack!!.setOnClickListener(this)
        tvDeleteTask!!.setOnClickListener(this)
        tvAddStop!!.setOnClickListener(this)
        tvClearStop!!.setOnClickListener(this)
        tvGetLocation!!.setOnClickListener(this)


        btnAddStopLocation!!.setOnClickListener(this)
        btnSave!!.setOnClickListener(this)


        prepareTickets()
        prepareCourier(AppConstants.ALL_COURIERS)
        prepareStopType()


        if (editMode) {
            sTicket.isEnabled = false
            btnSave.text = context!!.getString(R.string.update)
            tvTaskDetails!!.text = context!!.getString(R.string.task_details)
            tvDeleteTask!!.visibility = View.VISIBLE
            loadTaskData(AppConstants.CurrentSelectedTask)

        } else {
            defaultTaskData()
        }


    }

    private fun defaultTaskData() {

        sTicket.isEnabled = taskAddMode

        btnSave.text = context!!.getString(R.string.save)
        tvTaskDetails!!.text = context!!.getString(R.string.new_task)
        tvDeleteTask!!.visibility = View.INVISIBLE
        sTicket.setText(context!!.getString(R.string.select_ticket))
        etTaskName.setText(context!!.getString(R.string.task_name))
        sCourier.setText(context!!.getString(R.string.select_courier))
        etAmount!!.setText(context!!.getString(R.string.amount))

        rvStops.adapter = null
        rlStops.visibility = View.GONE

        taskAddMode = false
    }

    private fun loadTaskData(task: Task) {

        sTicket.setText(AppConstants.CurrentSelectedTicket.TicketName)

        selectedTicket =
            AppConstants.GetALLTicket.find { it.TicketId == AppConstants.CurrentSelectedTicket.TicketId }!!

        if (!task.Task.trim().isNullOrEmpty())
            etTaskName.setText(task.Task)
        if (task.CourierID != null) {
            sCourier.setText(task.CourierName)
            selectedCourier = AppConstants.ALL_COURIERS.find { it.CourierId == task.CourierID }!!
        }
        if (!task.Amount.toString().trim().isNullOrEmpty())
            etAmount!!.setText(task.Amount.toString())

        if (task.stopsmodel.count() > 0) {

            stopsList = task.stopsmodel

            loadTaskStops(task.stopsmodel)
//            rlStops.visibility = View.VISIBLE

        }

//        prepareTaskModelData(AppConstants.CurrentSelectedTask)
    }

    private fun prepareTaskModelData(task: Task) {

        taskModel.taskName = task.Task
        taskModel.amount = task.Amount
        taskModel.ticketID = task.TicketId
        taskModel.courierId = task.CourierID

        if (!editMode) { // new
            taskModel.addedBy = task.AddedBy

        } else { //edit
            taskModel.taskId = task.TaskId
            taskModel.modifiedBy = task.AddedBy
        }

        task.stopsmodel.forEach {

            if (it.status == 0) // new
            {
                taskModel.stopsmodels!!.add(
                    Stopsmodel(
                        it.StopName,
                        it.Latitude!!,
                        it.Longitude!!,
                        it.addedBy,
                        it.StopTypeID
                    )
                )
            }
        }

    }

    private fun validateAll(): Boolean {

        if (etTaskName.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Task Name is required.")
            AnimateScroll.scrollToView(scroll, etTaskName)
            etTaskName.requestFocus()
            return false
        } else if (etAmount.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Amount is required.")
            AnimateScroll.scrollToView(scroll, etAmount)
            etAmount.requestFocus()
            return false
        } else if (rlStops.isVisible &&
            etLatitude.text.toString().isNotEmpty() || etLongitude.text.toString().isNotEmpty()
        ) {

            Alert.showMessage(context!!, "Complete add Stop data.")
            AnimateScroll.scrollToView(scroll, etLongitude)
            btnAddStopLocation.requestFocus()
            return false
        }


        return true

    }

    private fun prepareTaskData() {
        var taskName = etTaskName.text.toString()
        var amount = etAmount.text.toString().toDouble()
        var addedBY = AppConstants.CurrentLoginAdmin.AdminId
        var ticketId = AppConstants.CurrentSelectedTicket.TicketId
        var taskId = AppConstants.CurrentSelectedTask.TaskId
        var courierId = selectedCourier.CourierId ?: null

        task = Task(taskName, amount, addedBY, ticketId, taskId, courierId!!, stopsList)
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


    private fun editTask(taskModel: TaskModel) {
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
                            Alert.hideProgress()

                            var tasks = response.ResponseObj!!
                            var task = tasks[0]
                            var selectedTaskIndex =
                                AppConstants.CurrentSelectedTicket.taskModel.indexOf(AppConstants.CurrentSelectedTicket.taskModel.find { it.TaskId == task.TaskId })
                            AppConstants.CurrentSelectedTicket.taskModel[selectedTaskIndex] = task
                            AppConstants.CurrentSelectedTask = task

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
                    AppConstants.CurrentSelectedTicket=ticket
                } else
                    selectedTicket = Ticket("0", getString(R.string.select_ticket))

            }

        sTicket.setOnClickListener(View.OnClickListener {
            sTicket.showDropDown()

        })

        // set current ticket
        sTicket.setText(AppConstants.CurrentSelectedTicket.TicketName)

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
//                    sCourier.setText(AppConstants.ALL_COURIERS.find { it.CourierId == selectedCourier.CourierId }!!.name)
                    sCourier.setText(courier.name)
                } else {
//                    sCourier.setText(getString(R.string.select_courier))
                    selectedCourier = Courier(0, getString(R.string.select_courier))
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
        stopsModelList.add(
            Stopsmodel(
                stop.StopName,
                stop.Latitude!!,
                stop.Longitude!!,
                stop.addedBy,
                stop.StopTypeID
            )
        )

        var adapter = StopAdapter(context!!, stopsList, this)
        rvStops.adapter = adapter
        rvStops.layoutManager =
            LinearLayoutManager(AppController.getContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter.notifyDataSetChanged()

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

    private fun deleteTask(task: Task) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.removeTask(task.TaskId, AppConstants.CurrentLoginAdmin.AdminId)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Task>>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            context!!.getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Task>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
                            var tasks = response.ResponseObj!!
                            AppConstants.CurrentSelectedTicket.taskModel = tasks
                            //  close and go to task details view
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

        jResult.putOpt("TaskName", task.Task)
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
//endregion


}
