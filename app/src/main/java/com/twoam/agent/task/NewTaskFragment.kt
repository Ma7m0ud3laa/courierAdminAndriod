package com.twoam.agent.task


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
import com.twoam.agent.model.Courier
import com.twoam.agent.model.Stop
import com.twoam.agent.model.Task
import com.twoam.agent.model.Ticket
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


class NewTaskFragment : BaseFragment(), IBottomSheetCallback, View.OnClickListener {

    //region Members
    private var ticketList = ArrayList<Ticket>()
    private var tasks = ArrayList<Task>()
    private var stopsList = ArrayList<Stop>()
    private var courierList = ArrayList<Courier>()
    private var dummyCourierList = ArrayList<Courier>()
    private var dummyTicketList = ArrayList<Ticket>()
    private var selectedStop = Stop()
    private var selectedCourier = Courier()
    private var selectedTicket = Ticket()
    private var selectedStopType: StopType? = null


    private var task = Task()
     var editMode = false
    private lateinit var currentView: View
    private lateinit var scroll: ScrollView
    private var btnBack: ImageView? = null
    private var tvTaskDetails: TextView? = null
    private var tvDelete: TextView? = null
    private var listener: IBottomSheetCallback? = null
    private lateinit var etTaskName: EditText
    private lateinit var etAmount: EditText
    private lateinit var sTicket: Spinner
    private lateinit var sCourier: Spinner
    private lateinit var tvAddStop: TextView
    private lateinit var tvClearStop: TextView
    private lateinit var tvGetLocation: TextView


    private lateinit var btnAddStopLocation: ImageButton
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
//        init()
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
        } else {
            throw ClassCastException("$context must implement IBottomSheetCallback.onBottomSheetSelectedItem")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBottomSheetSelectedItem(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
            R.id.btnBack -> {
                if(editMode)
                    listener!!.onBottomSheetSelectedItem(3)
                else
                listener!!.onBottomSheetSelectedItem(0)
            }
            R.id.tvAddStop -> {
                if (!rlStops.isVisible)
                    rlStops.visibility = View.VISIBLE

            }

            R.id.tvClearStop -> {
                if (rlStops.isVisible) {
                    // todo  empty stop list
                    rlStops.visibility = View.GONE

                    stopsList.clear()
                    rvStops.adapter = null

                }

            }

            R.id.tvGetLocation -> {
                //to do open map and get selected location
                listener!!.onBottomSheetSelectedItem(8)
            }

            R.id.btnAddStopLocation -> {
                if (validateStopData()) {
                    var stopName = etStopName.text.toString()
                    var stopTypeId = sStopType.selectedItemPosition + 1
                    var stopType = sStopType.selectedItem.toString()
                    var latitude = etLatitude.text.toString().toDouble()
                    var longitude = etLongitude.text.toString().toDouble()

                    var stop = Stop(
                        task.TaskId,
                        AppConstants.CurrentLoginAdmin.AdminId,
                        stopName,
                        latitude,
                        longitude,
                        stopTypeId,
                        stopType,
                        ""
                    )
                    addStopToStopList(stop)
                }

            }


            R.id.btnSave -> {
                //todo save task to ticket
                if (validateAll()) {
                    prepareTaskData()
                    addTask(task)
                }

            }
        }
    }

    //endregion


    //region Helper Functions
    private fun init() {
        scroll = currentView.findViewById(R.id.scroll)
        btnBack = currentView!!.findViewById(R.id.btnBack)
        tvTaskDetails = currentView!!.findViewById(R.id.tvTaskDetails)
        tvDelete = currentView!!.findViewById(R.id.tvDelete)
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

        btnBack!!.setOnClickListener(this)
        tvDelete!!.setOnClickListener(this)
        tvAddStop!!.setOnClickListener(this)
        tvClearStop!!.setOnClickListener(this)
        tvGetLocation!!.setOnClickListener(this)


        btnAddStopLocation!!.setOnClickListener(this)
        btnSave!!.setOnClickListener(this)

//        getAllCouriers()

        tvTaskDetails!!.text = context!!.getString(R.string.new_task)
        prepareTickets()
        prepareCourier(AppConstants.ALL_COURIERS)
        prepareStopType()


        if (editMode) {
            btnSave.setText(context!!.getString(R.string.update))
            tvTaskDetails!!.text = context!!.getString(R.string.task_details)
            tvDelete!!.visibility = View.VISIBLE
//            getAllTaskStops(AppConstants.CurrentSelectedTask)
            loadTaskData(AppConstants.CurrentSelectedTask)
        }


    }

    private fun loadTaskData(task:Task)
    {
        selectValue(sTicket,task.TicketId)
        selectValue(sCourier,task.CourierId)
        if(!task.Task.trim().isNullOrEmpty())
        etTaskName.setText(task.Task)
        if(!task.Amount.toString().trim().isNullOrEmpty())
        etAmount!!.setText(task.Amount.toString())

        if(task.stopsmodel.count()>0)
        {
            loadTaskStops(task.stopsmodel)
            rlStops.visibility=View.VISIBLE

        }
    }

    private fun selectValue(spinner: Spinner, value: Any) {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i) == value) {
                spinner.setSelection(i)
                break
            }
        }
    }


    private fun validateAll(): Boolean {


        if (selectedTicket.TicketId.trim().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Please select ticket to assign task to it.")
            AnimateScroll.scrollToView(scroll, sTicket)
            sTicket.isFocusable = true
            sTicket.isFocusableInTouchMode = true
            sTicket.requestFocus()
            return false
        } else if (etTaskName.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Task Name is required.")
            AnimateScroll.scrollToView(scroll, etTaskName)
            etTaskName.requestFocus()
            return false
        } else if (etAmount.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Amount is required.")
            AnimateScroll.scrollToView(scroll, etAmount)
            etAmount.requestFocus()
            return false
        } else if (sTicket.selectedItem.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Status is required.")
            AnimateScroll.scrollToView(scroll, sTicket)
            sTicket.requestFocus()
            return false
        } else if (rlStops.isVisible &&
            etLatitude.text.toString().isNotEmpty() || etLongitude.text.toString().isNotEmpty()
        ) {

            Alert.showMessage(context!!, "Complete add Stop data.")
            AnimateScroll.scrollToView(scroll, etLongitude)
            btnAddStopLocation.requestFocus()
            return false
        }
//        else if (etLatitude.text.toString().isNullOrEmpty()) {
//            Alert.showMessage(context!!, "Latitude is required.")
//            AnimateScroll.scrollToView(scroll, etLatitude)
//            etLatitude.requestFocus()
//            return false
//        } else if (etLongitude.text.toString().isNullOrEmpty()) {
//            Alert.showMessage(context!!, "Longitude is required.")
//            AnimateScroll.scrollToView(scroll, etLongitude)
//            etLongitude.requestFocus()
//            return false
//        }


        return true

    }

    private fun prepareTaskData() {
        var taskName = etTaskName.text.toString()
        var amount = etAmount.text.toString().toDouble()
        var addedBY = AppConstants.CurrentLoginAdmin.AdminId
        var ticketId = selectedTicket.TicketId.toString()
        var taskId = AppConstants.CurrentSelectedTask.TaskId
        var courierId = selectedCourier.CourierId

        task = Task(taskName, amount, addedBY, ticketId, taskId, courierId, stopsList)
    }

    private fun addTask(task: Task) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.addTask(
                task.Task,
                task.Amount, task.AddedBy, task.TicketId, task.CourierId
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
                            tasks = response.ResponseObj!!
                            saveStopsToTask(stopsList)

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

    private fun removeTask(task: Task) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.removeTask(
                task.TaskId,
                task.AddedBy
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
                            tasks = response.ResponseObj!!

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

    private fun getPickupLoaction() {}

    private fun getDropOffLoaction() {}

    private fun addStop(stop: Stop) {

        var request = NetworkManager().create(ApiServices::class.java)
        var endPoint = request.addTaskStop(
            task.TaskId,
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
                stop.id, AppConstants.CurrentLoginAdmin.AdminId
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

        var adapter =
            TicketListAdapter(
                context!!,
                android.R.layout.simple_spinner_dropdown_item,
                dummyTicketList
            )
        sTicket.adapter = adapter

        sTicket.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {

                var ticket = parent.selectedItem as Ticket
                if (ticket.TicketId != "0")
                    selectedTicket = ticket
                else
                    selectedTicket = Ticket("0", getString(R.string.select_ticket))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun prepareCourier(courierList: ArrayList<Courier>) {
        if (courierList.size > 0) {
            dummyCourierList.clear()
            var newArray = courierList

            var firstItem = Courier(0, getString(R.string.select_courier))
            dummyCourierList.add(firstItem)

            for (c in newArray.indices) {
                dummyCourierList.add(newArray[c])
            }
        }


        var adapter = CourierListAdapter(
            context!!,
            android.R.layout.simple_spinner_dropdown_item,
            dummyCourierList
        )
        sCourier.adapter = adapter

        sCourier.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {

                var courier = parent.selectedItem as Courier
                if (courier.CourierId > 0)
                    selectedCourier = courier
                else
                    selectedCourier = Courier(0, getString(R.string.select_courier))

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun loadTaskStops(stopList: ArrayList<Stop>) {
        var adapter = StopAdapter(context!!, stopList)
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

    private fun saveStopsToTask(stopList: ArrayList<Stop>) {
        var counter = 0
        if (stopsList.size > 0) {
            if (NetworkManager().isNetworkAvailable(context!!)) {
                stopList.forEach {
                    addStop(it)
                    counter += 1
                }
            } else {
                Alert.showMessage(context!!, getString(R.string.no_internet))
            }
        }
        listener!!.onBottomSheetSelectedItem(10)
    }

    private fun addStopToStopList(stop: Stop) {
        stopsList.add(stop)

        var adapter = StopAdapter(context!!, stopsList)
        rvStops.adapter = adapter
        rvStops.layoutManager =
            LinearLayoutManager(AppController.getContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter.notifyDataSetChanged()

        clearStopControlsData()

    }

    private fun clearStopControlsData() {

        sStopType.setSelection(0)
        etStopName.setText("")
        etStopName.setHint(context!!.getString(R.string.stop_name))
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
//endregion


}
