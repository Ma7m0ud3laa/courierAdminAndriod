package com.kadabra.agent.task


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.kadabra.Networking.INetworkCallBack
import com.kadabra.Networking.NetworkManager
import com.kadabra.Utilities.Base.BaseFragment
import com.kadabra.agent.R
import com.kadabra.agent.adapter.*
import com.kadabra.agent.api.ApiResponse
import com.kadabra.agent.api.ApiServices
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.callback.ITaskCallback
import com.kadabra.agent.firebase.FirebaseManager
import com.kadabra.agent.model.*
import com.kadabra.agent.utilities.*
import com.kadabra.agent.utilities.Alert.hideProgress
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil


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
    private lateinit var btnCancel: Button
    private lateinit var tvStatus: TextView
    private lateinit var ivDirection: ImageView
    private lateinit var ivTaskImage: ImageView
    private lateinit var refresh: SwipeRefreshLayout
    private var dateValue = ""
    private var isRecording = false
    private val recordPermission = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_CODE = 21
    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: String = ""
    private lateinit var ivRecord: ImageView
    private lateinit var cbRecord: CheckBox
    private lateinit var record_timer: Chronometer
    private var allFiles = ArrayList<File>()
    private var recordPath = ""
    private var alertDialog: AlertDialog? = null
    private var cancelTaskView: View? = null
    private lateinit var ivBackConfirmCancel: ImageView
    private lateinit var etTotalCost: EditText
    private lateinit var btnCancelConfirm: Button
    private var mGeoApiContext: GeoApiContext? = null
    var meters = 0L
    private var totalKilometers = 0
    private var totalDuration: Float = 0F
    var totalDistance = 0L
    var totalSeconds = 0L
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
        initDierction()
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

    override fun onStop() {
        super.onStop()
        if (isRecording) {
            stopRecording()
        }
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

            R.id.ivTaskImage -> {
                listener!!.onBottomSheetSelectedItem(18)
            }
            R.id.tvDeleteTask -> {

                // if (!AppConstants.CurrentSelectedTask.TaskId.isNullOrEmpty() && AppConstants.CurrentSelectedTask.Status != AppConstants.IN_PROGRESS) {
                if (AppConstants.CurrentSelectedTask.Status == AppConstants.NEW) {
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
                if (
                    AppConstants.CurrentSelectedTask.Status != AppConstants.COMPLETED &&
                    AppConstants.CurrentSelectedTask.Status != AppConstants.CANCELLED
                ) {
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
                if (AppConstants.CurrentSelectedTask.Status != AppConstants.COMPLETED &&
                    AppConstants.CurrentSelectedTask.Status != AppConstants.CANCELLED
                ) {
                    Log.d(TAG, AppConstants.CurrentSelectedTask.Status)
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
                if (
                    AppConstants.CurrentSelectedTask.Status != AppConstants.COMPLETED &&
                    AppConstants.CurrentSelectedTask.Status != AppConstants.CANCELLED
                ) {
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
                if (validateAll()) {
                    hideKeyboard(btnSave)
                    prepareTaskData()
                    if (!editMode) {
                        addTask(taskModel)
                    } else if (editMode &&
                        AppConstants.CurrentSelectedTask.Status != AppConstants.CANCELLED &&
                        AppConstants.CurrentSelectedTask.Status != AppConstants.COMPLETED
                    )
                        editTask(taskModelEdit)
                    else
                        Alert.showMessage(
                            "Can't edit this task."
                        )

                }


            }

            R.id.btnCancel ->  // cancel task
            {
                if (
                    AppConstants.CurrentSelectedTask.Status != AppConstants.COMPLETED &&
                    AppConstants.CurrentSelectedTask.Status != AppConstants.CANCELLED
                ) {

                    if (alertDialog == null) {
                        var alert = AlertDialog.Builder(context!!)
                        alertDialog = alert.create()
                        alertDialog!!.setView(cancelTaskView)
                        etTotalCost.text.clear()
                        alertDialog!!.show()
                    } else {
                        etTotalCost.text.clear()
                        etTotalCost!!.requestFocus()
                        alertDialog!!.setView(cancelTaskView)
                        alertDialog!!.show()
                    }

                } else
                    Alert.showMessage(
                        "Can't Cancel this task."
                    )
            }

            R.id.ivBackConfirmCancel -> {
                if (alertDialog != null)
                    alertDialog!!.dismiss()
            }

            R.id.btnCancelConfirm -> {
                if (AppConstants.CurrentSelectedTask.TaskId.isNullOrEmpty() ||
                    AppConstants.CurrentSelectedTask.Status != AppConstants.COMPLETED
                ) {
                    var total = etTotalCost.text.toString().toDouble()
                    cancelTask(AppConstants.CurrentSelectedTask.TaskId, total)
                } else
                    Alert.showMessage(
                        "Can't Cancel this task."
                    )
            }

            R.id.ivDirection -> //get current courier direction if task is accepted
            {
                if (!AppConstants.CurrentSelectedTask.TaskId.isNullOrEmpty())
                    listener?.onBottomSheetSelectedItem(17)
                // the task is new so use it for calculate the distance between pickup and drop off before save task to decide create task for client or not
                //accrding to calculated kilometers
                else {
                    //make sure stops have pickup and dropOff
                    //get the kilometers and show on popup
                    if (validateTaskAcceptance()) {
                        var firstStop = stopsList.find { it.StopTypeID == 1 }
                        var lastStop = stopsList.find { it.StopTypeID == 2 }
                        var pickUp = LatLng(
                            firstStop!!.Latitude!!,
                            firstStop!!.Longitude!!
                        )
                        var dropOff = LatLng(
                            lastStop!!.Latitude!!,
                            lastStop!!.Longitude!!
                        )

                        calculateDirections(pickUp, dropOff)

                    }
                }


            }


            R.id.ivRecord -> //get current courier direction if task is accepted
            {

                if (isRecording) { //Stop Recording
                    stopRecording()
                    // Change button image and set Recording state to false
                    ivRecord!!.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.record_btn_stopped,
                            null
                        )
                    )
                    isRecording = false
                } else { //Check permission to record audio
                    if (checkPermissions()) { //Start Recording
                        startRecording()
                        // Change button image and set Recording state to false
                        ivRecord!!.setImageDrawable(
                            resources.getDrawable(
                                R.drawable.record_btn_recording,
                                null
                            )
                        )
                        isRecording = true
                    }
                }
            }

            R.id.cbRecord -> {
                if (AppConstants.CurrentSelectedTask.TaskId.isNullOrEmpty() || AppConstants.CurrentSelectedTask.Status != AppConstants.COMPLETED) {
                    if (cbRecord.isChecked) {
                        ivRecord.visibility = View.VISIBLE
                        record_timer.visibility = View.VISIBLE


                    } else {
                        ivRecord.visibility = View.INVISIBLE
                        record_timer.visibility = View.INVISIBLE
                        recordFile = ""
                        recordPath = ""
                    }
                } else
                    Alert.showMessage(

                        "Can't edit this task."
                    )
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
        ivTaskImage = currentView!!.findViewById(com.kadabra.agent.R.id.ivTaskImage)
//        mRipplePulseLayout = currentView!!.findViewById(com.kadabra.agent.R.id.layout_ripplepulse)

        ivRecord = currentView!!.findViewById(com.kadabra.agent.R.id.ivRecord)
        cbRecord = currentView!!.findViewById(com.kadabra.agent.R.id.cbRecord)
        record_timer = currentView!!.findViewById(com.kadabra.agent.R.id.record_timer)

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
        btnCancel = currentView!!.findViewById(com.kadabra.agent.R.id.btnCancel)

        cancelTaskView = View.inflate(context!!, R.layout.cancel_task_layout, null)
        ivBackConfirmCancel = cancelTaskView!!.findViewById(R.id.ivBackConfirmCancel)
        etTotalCost = cancelTaskView!!.findViewById(R.id.etTotalCost)
        btnCancelConfirm = cancelTaskView!!.findViewById(R.id.btnCancelConfirm)


//        mRipplePulseLayout.startRippleAnimation()

        ivBack!!.setOnClickListener(this)
        tvDeleteTask!!.setOnClickListener(this)
        tvAddStop!!.setOnClickListener(this)
        tvClearStop!!.setOnClickListener(this)
        tvGetLocation!!.setOnClickListener(this)
        etPickupTime.setOnClickListener(this)
        ivDirection.setOnClickListener(this)
        ivRecord.setOnClickListener(this)
        cbRecord.setOnClickListener(this)
        ivTaskImage.setOnClickListener(this)
        btnAddStopLocation!!.setOnClickListener(this)
        btnSave!!.setOnClickListener(this)
        btnCancel!!.setOnClickListener(this)
        ivBackConfirmCancel.setOnClickListener(this)
        btnCancelConfirm.setOnClickListener(this)




        refresh.setOnRefreshListener {
            if (editMode)
                getTaskDetails(AppConstants.CurrentSelectedTask.TaskId)
            else
                refresh.isRefreshing = false
        }

    }

    private fun defaultTaskData() {

        btnSave.text = context!!.getString(com.kadabra.agent.R.string.save)
        btnCancel.visibility = View.INVISIBLE
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

        if (task.Status == AppConstants.COMPLETED || task.Status == AppConstants.CANCELLED) {
            btnSave.isEnabled = false
            btnCancel.visibility = View.INVISIBLE
            btnSave.setBackgroundResource(R.drawable.rounded_button_disenaple)
        }

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
            Alert.showMessage("TaskName Name is required.")
            AnimateScroll.scrollToView(scroll, etTaskName)
            etTaskName.requestFocus()
            return false
        } else if (etTaskDescription.text.toString().isNullOrEmpty()) {
            Alert.showMessage("TaskName Description is required.")
            AnimateScroll.scrollToView(scroll, etTaskDescription)
            etTaskDescription.requestFocus()
            return false
        }
//        else if (etAmount.text.toString().isNullOrEmpty()) {
//            Alert.showMessage(  "Amount is required.")
//            AnimateScroll.scrollToView(scroll, etAmount)
//            etAmount.requestFocus()
//            return false
//        }
        else if (etPickupTime.text.toString().isNullOrEmpty()) {
            Alert.showMessage("Pickup Time is required.")
            AnimateScroll.scrollToView(scroll, etPickupTime)
            etPickupTime.requestFocus()
            showDateTimePicker()
            return false
        } else if (selectedCourier.CourierId == 0) {
            Alert.showMessage("Courier is required.")
            AnimateScroll.scrollToView(scroll, sCourier)
            sCourier.requestFocus()
//            sCourier.isCursorVisible=true
            sCourier.showDropDown()
            return false
        } else if (rlStops.isVisible && stopsList.size <= 0 &&
            etLatitude.text.toString().isNotEmpty() || etLongitude.text.toString().isNotEmpty()
        ) {

            Alert.showMessage("Complete add Stop data.")
            AnimateScroll.scrollToView(scroll, etLongitude)
            btnAddStopLocation.requestFocus()
            return false
        } else if (stopsList.size < 2) {
            Alert.showMessage("Cant save task without Pickup and Drop off stops.")
            rlStops.visibility = View.VISIBLE
            AnimateScroll.scrollToView(scroll, etStopName)
            etStopName.requestFocus()

            return false
        } else if (stopsList.size >= 2 && pick == 0 || drop == 0) {
            AnimateScroll.scrollToView(scroll, etStopName)
            etStopName.requestFocus()
            if (pick < 1)
                Alert.showMessage(

                    "The task must include one Pickup Stop ."
                )
            if (drop < 1)
                Alert.showMessage(

                    "The task must include one Drop Off Stop ."
                )


            return false
        }



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

                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Task>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {

                            var tasks = response.ResponseObj!!
                            var task = tasks[0]
                            AppConstants.CurrentSelectedTicket.taskModel.add(task)
                            var recordFullPath = "$recordPath/$recordFile"
                            if (!recordFile.isNullOrEmpty())
                                uploadRecord(recordFullPath, task.TaskId)
                            else
                                listener!!.onBottomSheetSelectedItem(3)


                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                response.Message
//                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                response.Message
//                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(getString(R.string.no_internet))
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


                            var recordFullPath = "$recordPath/$recordFile"
                            if (!recordFile.isNullOrEmpty())
                                uploadRecord(recordFullPath, task.TaskId)
                            else
                                listener!!.onBottomSheetSelectedItem(3)

                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(

                                response.Message
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                response.Message
//                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(getString(R.string.no_internet))
        }
    }


    private fun cancelTask(taskId: String, amount: Double) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.cancelTask(
                taskId,
                amount
            )
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<Boolean?>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(

                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Boolean?>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Log.d(TAG, "response - " + response.toString())
                            Log.d(TAG, "response.Status - " + response.Status.toString())
                            alertDialog!!.dismiss()
                            listener!!.onBottomSheetSelectedItem(3)

                        } else {
                            Alert.hideProgress()
                            Alert.showMessage(
                                response.Message
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(getString(R.string.no_internet))
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

                        getString(R.string.error_login_server_error)
                    )
                }

                override fun onSuccess(response: ApiResponse<ArrayList<Stop>>) {
                    if (response.Status == AppConstants.STATUS_SUCCESS) {
                        stopsList = response.ResponseObj!!

                    } else if (response.Status == AppConstants.STATUS_FAILED) {
                        Alert.showMessage(

                            getString(R.string.error_login_server_error)
                        )
                    } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                        Alert.showMessage(

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

                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(

                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(getString(R.string.no_internet))
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

                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(

                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(getString(R.string.no_internet))
        }
    }

    private fun getAllCouriers() {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getAllCouriersWithStatus()
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Courier>>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(

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

                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(

                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(getString(R.string.no_internet))
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

                                getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(

                                getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(getString(R.string.no_internet))
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

//        sTicket.setOnClickListener(View.OnClickListener {
//            sTicket.showDropDown()
//
//        })

//        // set current ticket
//        sTicket.setText(AppConstants.CurrentSelectedTicket.TicketName)

    }

    private fun prepareCourier(courierList: ArrayList<Courier>) {
        dummyCourierList.clear()
        if (courierList.size > 0) {
            var newArray = courierList

//            var firstItem = Courier(0, getString(R.string.select_courier))
//            dummyCourierList.add(firstItem)

            for (c in newArray.indices) {
                dummyCourierList.add(newArray[c])
            }
        }


        courierList?.sortBy { it.CourierName }
        adapterCourier = CourierListAdapter(
            context!!,
            android.R.layout.simple_dropdown_item_1line,
            courierList!!
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
                    sCourier.hint = getString(R.string.select_courier)
                }


            }

//        sCourier.setOnClickListener(View.OnClickListener {
//            var filter = adapterCourier?.filter
//            filter = null
////            sCourier.filters=filter
//            sCourier.showDropDown()
//
//        })

        sCourier.setOnTouchListener { v, event ->
            if (courierList.size > 0) {
                if (sCourier.text.trim().isNotEmpty())
                    adapterCourier!!.filter.filter(null)
                sCourier.showDropDown()
            }

            false
        }


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
                Alert.showMessage(getString(R.string.no_internet))
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
        Alert.showMessage("Stop ${stop.StopName} is added successfully.")
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
            Alert.showMessage("Stop Name is required.")
            AnimateScroll.scrollToView(scroll, etStopName)
            etStopName.requestFocus()
            return false
        } else if (etLatitude.text.toString().isNullOrEmpty()) {
            Alert.showMessage("Latitude is required.")
            AnimateScroll.scrollToView(scroll, etLatitude)
            etLatitude.requestFocus()
            return false
        } else if (etLongitude.text.toString().isNullOrEmpty()) {
            Alert.showMessage("Longitude is required.")
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
                object : INetworkCallBack<ApiResponse<Task?>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(

                            context!!.getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Task?>) {
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
                                response.Message
                                // "Can't delete this task it's in progress."
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(

                                context!!.getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(context!!.getString(R.string.no_internet))
        }
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

                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Task>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {

                            task = response.ResponseObj!!
                            AppConstants.CurrentSelectedTask = task
                            FirebaseManager.getTaskImage(task.TaskId) { success, data ->

                                if (success) {
                                    AppConstants.CURRENT_IMAGE_URI = data
                                    Glide.with(activity!! /* context */)
                                        .load(data)
                                        .into(ivTaskImage)
                                    ivTaskImage.visibility = View.VISIBLE
                                }
                            }
                            loadTaskData(task)
                            hideProgress()
                            refresh.isRefreshing = false
                        } else {
                            hideProgress()
                            refresh.isRefreshing = false
                            Alert.showMessage(

                                getString(R.string.error_network)
                            )
                        }

                    }
                })

        } else {
            hideProgress()
            refresh.isRefreshing = false
            Alert.showMessage(
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

            var datePickerDialog = DatePickerDialog(
                context!!,
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
                context!!,
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

    private fun checkPermissions(): Boolean { //Check permission
        return if (ActivityCompat.checkSelfPermission(
                context!!,
                recordPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) { //Permission Granted
            true
        } else { //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(recordPermission),
                PERMISSION_CODE
            )
            false
        }
    }

    private fun stopRecording() { //Stop Timer, very obvious
        record_timer!!.stop()
        //Change text on page to file saved
//        Alert.showMessage(context!!, "Recording Stopped, File Saved : $recordFile")
//        filenameText!!.text = "Recording Stopped, File Saved : $recordFile"
        //Stop media recorder and set it to null for further use to record new audio
        record_timer.text = "Done."
        mediaRecorder!!.stop()
        mediaRecorder!!.release()
        mediaRecorder = null
    }

    private fun startRecording() { //Start timer from 0
        record_timer!!.base = SystemClock.elapsedRealtime()
        record_timer!!.start()
        //Get app external directory path
        recordPath = activity!!.getExternalFilesDir("/")?.absolutePath!!
        //Get current date and time
        val formatter =
            SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA)
        val now = Date()
        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + formatter.format(now) + ".mp3"

//        Alert.showMessage(context!!, "Recording, File Name : $recordFile")
//        filenameText!!.text = "Recording, File Name : $recordFile"
        //Setup Media Recorder for recording
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder!!.setOutputFile("$recordPath/$recordFile")
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder!!.setMaxDuration(120000)

        try {
            mediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //Start Recording
        mediaRecorder!!.start()
    }
//endregion


    private fun uploadRecord(filePath: String, taskId: String) {
        if (cbRecord.isChecked && !filePath.trim().isNullOrEmpty()) {
            Log.d(TAG, filePath)

            var newFile = "$recordPath/$taskId.mp3"

            Log.d(TAG, newFile)
            var done = rename(File(filePath), File(newFile))
            Log.d(TAG, done.toString())
            if (done) {
                var uri = Uri.fromFile(File(newFile))
                Log.d(TAG, uri.toString())
                FirebaseManager.uploadRecord(uri, taskId) { success ->
                    if (success) {
                        Alert.hideProgress()
                        Log.d(TAG, "Success")
                        listener!!.onBottomSheetSelectedItem(3)
                    } else {
                        Log.d(TAG, "Failed")
                        Alert.showMessage("Error on uploading file to server.")
                    }
                }
            }
        }


    }


    private fun rename(from: File, to: File): Boolean {

        return from.parentFile.exists() && from.exists() && from.renameTo(to)
    }

    //this is eused when we need to check the kilmeters for the task and till the client about it before screate the task
//    if accpted save the task
    private fun validateTaskAcceptance(): Boolean {
        var pick = stopsList.count { it.StopTypeID == 1 }
        var drop = stopsList.count { it.StopTypeID == 2 }
        if (rlStops.isVisible && stopsList.size <= 0 &&
            etLatitude.text.toString().isNotEmpty() || etLongitude.text.toString().isNotEmpty()
        ) {

            Alert.showMessage("Complete add Stop data.")
            AnimateScroll.scrollToView(scroll, etLongitude)
            btnAddStopLocation.requestFocus()
            return false
        } else if (stopsList.size < 2) {
            Alert.showMessage("for calculate task kilometers please enter stops first and click again.")
            rlStops.visibility = View.VISIBLE
            AnimateScroll.scrollToView(scroll, etStopName)
            etStopName.requestFocus()

            return false
        } else if (stopsList.size >= 2 && pick == 0 || drop == 0) {
            AnimateScroll.scrollToView(scroll, etStopName)
            etStopName.requestFocus()
            if (pick < 1)
                Alert.showMessage(

                    "The task must include one Pickup Stop ."
                )
            if (drop < 1)
                Alert.showMessage(

                    "The task must include one Drop Off Stop ."
                )


            return false
        }
        return true
    }

    private fun calculateDirections(
        origin: LatLng,
        dest: LatLng
    ) {

        val destination = com.google.maps.model.LatLng(
            dest.latitude,
            dest.longitude
        )
        val directions = DirectionsApiRequest(mGeoApiContext)
        directions.alternatives(false)

        directions.origin(
            com.google.maps.model.LatLng(
                origin.latitude,
                origin.longitude
            )
        )
        Log.d(TAG, "calculateDirections: destination: $destination")

        if (stopsList.size > 2) {
            stopsList.forEach {

                if (it.StopTypeID == 3) {
                    directions.waypoints(
                        com.google.maps.model.LatLng(
                            it.Latitude!!,
                            it.Longitude!!
                        )
                    )

                }

            }
            directions.optimizeWaypoints(true)
        }

        directions.destination(destination)
            .setCallback(object : PendingResult.Callback<DirectionsResult?> {
                override fun onResult(result: DirectionsResult?) {

                    result!!.routes[0].legs.forEach {
                        Log.d(
                            TAG,
                            "LEG: duration: " + it.duration
                        );
                        Log.d(
                            TAG,
                            "LEG: distance: " + it.distance
                        );
                        Log.d("LEG DATA", it.toString())

                        meters += it.distance.inMeters
                        totalDistance += it.distance.inMeters
                        totalSeconds += it.duration.inSeconds
                    }
                    Log.d(TAG, "totalKilometers: $totalKilometers")
                    Log.d(TAG, "METERS:  $meters")
                    totalKilometers = conevrtMetersToKilometers(meters)
                    meters = 0L
                    var data =
                        "Total Kilometers: ( " + totalKilometers + " " + getString(R.string.km) + "  )"

                    activity?.runOnUiThread {
                        Alert.showAlertMessage(context!!, AppConstants.INFO, data)
                    }


                }

                override fun onFailure(e: Throwable) {
                    activity!!.runOnUiThread {
                        Alert.hideProgress()
                        Alert.showMessage(context!!, "Can't find a way there.")
                        totalKilometers = 0
                        Log.e(
                            TAG,
                            "calculateDirections: Failed to get directions: " + e.message
                        )
                    }
                }
            })

    }

    private fun initDierction() {
        //direction
        if (mGeoApiContext == null) {
            mGeoApiContext = GeoApiContext.Builder()
                .apiKey(getString(R.string.google_map_key))
                .build()
        }
    }

    private fun conevrtMetersToKilometers(meters: Long): Int {
        var kilometers = 0
        if (meters in 100..999) //grater than 100 meters and ess than 1000 meters consider as 1 kilometer
            kilometers = 1
        else
            kilometers = ceil((meters * 0.001).toFloat()).toInt()

        Log.d(TAG, "totalKilometers: RESULT $kilometers")


        return kilometers
    }
}
