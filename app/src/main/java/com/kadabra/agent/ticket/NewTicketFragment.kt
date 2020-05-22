package com.kadabra.agent.ticket

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kadabra.Networking.INetworkCallBack
import com.kadabra.Networking.NetworkManager
import com.kadabra.agent.R
import com.kadabra.agent.adapter.*
import com.kadabra.agent.api.ApiResponse
import com.kadabra.agent.api.ApiServices
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.callback.ITaskCallback
import com.kadabra.agent.model.*
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AnimateScroll
import com.kadabra.agent.utilities.AppConstants
import com.kadabra.agent.utilities.AppController
import com.reach.plus.admin.util.UserSessionManager
import android.widget.TextView
import android.widget.EditText
import com.kadabra.Utilities.Base.BaseFragment
import android.text.InputFilter
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hbb20.CountryCodePicker


class NewTicketFragment : BaseFragment(), IBottomSheetCallback, ITaskCallback,
    View.OnClickListener {

    private var TAG = this.javaClass.simpleName
    private lateinit var scroll: ScrollView
    private lateinit var rlParent: RelativeLayout

    private var tvTicketName: TextView? = null
    private var tvTicketDetails: TextView? = null
    private var tvStatus: TextView? = null
    private var tvPriority: TextView? = null
    private var tvClientName: TextView? = null
    private var tvPrice: TextView? = null
    private var ivCheck: ImageView? = null

    private var tvAddServiceCost: TextView? = null
    private var tvAddTask: TextView? = null
    private var tvTasks: TextView? = null
    private var rvTasks: RecyclerView? = null
    private var rvServiceCost: RecyclerView? = null
    private var currentView: View? = null
    private var serviceCostView: View? = null
    private lateinit var etTicketName: EditText
    private lateinit var etTicketDescription: EditText
    private lateinit var etMobile: EditText
    private var ccp: CountryCodePicker? = null

    //    private lateinit var sCategory: AutoCompleteTextView
    private lateinit var sPriority: AutoCompleteTextView
    private lateinit var sStatus: AutoCompleteTextView
    private lateinit var sPayment: AutoCompleteTextView
    //    private lateinit var cbNeedCourier: CheckBox
    private lateinit var ivBack: ImageView
    private lateinit var btnSave: Button
    private lateinit var refresh: SwipeRefreshLayout

    private var listener: IBottomSheetCallback? = null
    private var taskListener: ITaskCallback? = null
    private var ticketModel = TicketModel()
    private var ticket = Ticket()
    private var dummyTicketCategoryList = ArrayList<TicketCategory>()
    private var dummyTicketStatusList = ArrayList<TicketStatus>()
    private var dummyTicketPriorityList = ArrayList<TicketPriority>()
    private var dummyTicketPaymentMethodList = ArrayList<TicketPaymentMethod>()
    private var selectedCategory = TicketCategory("", "")
    private var selectedStatus = TicketStatus(null, "")
    private var selectedPriority = TicketPriority(null, "")
    private var selectedPaymentMethod = TicketPaymentMethod(null, "")
    private var ticketCategoryAdapter: TicketCategoryAdapter? = null
    private var ticketPriorityAdapter: TicketPriorityAdapter? = null
    private var ticketServiceCostAdapter: TicketServiceCostAdapter? = null
    private var ticketPaymentMethodAdapter: TicketPaymentMethodAdapter? = null
    private var ticketStatusAdapter: TicketStatusAdapter? = null
    private var btnSaveServiceCost: Button? = null
    private var ivBackServiceCost: ImageView? = null
    private var etServiceCost: EditText? = null
    private var etCost: EditText? = null
    private var alertDialog: AlertDialog? = null
    var editMode = false
    var taskInProgress = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(
            R.layout.fragment_new_ticket, container, false
        )

        init()

        if (NetworkManager().isNetworkAvailable(context!!)) {
            var subData = UserSessionManager.getInstance(context!!).getTicketSubData()
            if (subData != null) {
//                prepareCategories(subData.CategoriesModels!!)
                prepareStatus(subData.StatusModels!!)
                preparePriorities(subData.PrioritiesModels!!)
                preparePaymentMethods(subData.paymentMethodModels!!)
            } else {
                prepareTicketSubData()
            }
        } else
            Alert.showMessage(getString(R.string.no_internet))

//        if (editMode)
//            getTicketById(AppConstants.CurrentSelectedTicket.TicketId!!)
//            loadTicketDetails(AppConstants.CurrentSelectedTicket)

//        else
//            defaultTicketData()

        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivCheck -> {
                if (etMobile.text.trim().isNullOrEmpty()) {
                    Alert.showMessage("User Mobile is required.")
                    AnimateScroll.scrollToView(scroll, etMobile)
                    etMobile.requestFocus()
                    return
                } else {
                    if (validatePhone(ccp!!, etMobile)) {
                        var mobileNo = ccp?.fullNumber
                        getClientName(mobileNo!!)
                        Log.d(TAG, mobileNo)
                    } else {
                        Alert.showMessage("User Mobile is required.")
                        AnimateScroll.scrollToView(scroll, etMobile)
                        etMobile.requestFocus()
                    }

                }

            }
            R.id.tvAddServiceCost -> {// add task to the current ticket
                if (NetworkManager().isNetworkAvailable(context!!))
                    showServiceCostWindow()
                else
                    Alert.showMessage(getString(R.string.no_internet))
            }

            R.id.tvAddTask -> {// add task to the current ticket
                AppConstants.CurrentSelectedTask = Task()
                AppConstants.CurrentSelecedNotification = Notification()
                AppConstants.CurrentSelectedStop = Stop()
                listener!!.onBottomSheetSelectedItem(7)
            }

            R.id.ivBack -> {
                alertDialog = null //for clear the alert dialouge child contents
                listener!!.onBottomSheetSelectedItem(0)
            }

            R.id.ivBackServiceCost -> {
                //close dialouge
                if (alertDialog != null)
                    alertDialog!!.dismiss()
            }

            R.id.btnSaveServiceCost -> {

                if (NetworkManager().isNetworkAvailable(context!!)) {
                    if (alertDialog != null) {
                        if (validateServiceCost()) {
                            var serviceCost = TicketServiceCost(
                                etServiceCost!!.text.toString(),
                                etCost!!.text.toString().toDouble()
                            )
                            AppConstants.TICKET_SERVICE_COST_LIST.add(serviceCost)
                            prepareTicketServiceCost(AppConstants.TICKET_SERVICE_COST_LIST)
                            alertDialog!!.dismiss()

                        }
                    }
                } else
                    Alert.showMessage(getString(R.string.no_internet))

            }
            R.id.btnSave -> {
//                if (AppConstants.CurrentLoginAdmin.IsSuperAdmin) {
                if (NetworkManager().isNetworkAvailable(context!!)) {
                    if (validateAll()) {
                        prepareTicketData()
                        if (!editMode)
                            addTicket(ticketModel)
                        else
                            editTicket(ticketModel)
                    }
                } else
                    Alert.showMessage(getString(R.string.no_internet))
//                } else
//                    Alert. showMessage( "You are not authorized to perform this action!!.")


            }
        }

    }

    private fun validateServiceCost(): Boolean {

        if (etServiceCost!!.text.trim().toString().isNullOrEmpty()) {
            Alert.showMessage("Service cost Name is required.")
            etServiceCost!!.requestFocus()
            return false
        }
        if (etCost!!.text.trim().toString().isNullOrEmpty()) {
            Alert.showMessage("cost is required.")
            etCost!!.requestFocus()
            return false
        }
        return true
    }


    private fun init() {
        AppConstants.TICKET_SERVICE_COST_LIST.clear()
        scroll = currentView!!.findViewById(R.id.scroll)
        rlParent = currentView!!.findViewById(R.id.rlParent)

        refresh = currentView!!.findViewById(R.id.refresh)

        tvTicketName = currentView!!.findViewById(R.id.tvTicketDetails)
        tvTicketDetails = currentView!!.findViewById(R.id.tvTicketDetails)
        tvClientName = currentView!!.findViewById(R.id.tvClientName)
        ivCheck = currentView!!.findViewById(R.id.ivCheck)
        tvStatus = currentView!!.findViewById(R.id.tvStatus)
        tvPriority = currentView!!.findViewById(R.id.tvPriority)
        tvAddServiceCost = currentView!!.findViewById(R.id.tvAddServiceCost)
        tvAddTask = currentView!!.findViewById(R.id.tvAddTask)
        tvTasks = currentView!!.findViewById(R.id.tvTasks)

        ivBack = currentView!!.findViewById(R.id.ivBack)
        rvServiceCost = currentView!!.findViewById(R.id.rvServiceCost)
        rvTasks = currentView!!.findViewById(R.id.rvTasks)
        etTicketName = currentView!!.findViewById(R.id.etTicketName)
        etTicketDescription = currentView!!.findViewById(R.id.etTicketDescription)
        etMobile = currentView!!.findViewById(R.id.etMobile)
        ccp = currentView!!.findViewById(R.id.ccp)

//        sCategory = currentView!!.findViewById(R.id.sCategory)
        sPriority = currentView!!.findViewById(R.id.sPriority)
        sStatus = currentView!!.findViewById(R.id.sStatus)
        sPayment = currentView!!.findViewById(R.id.sPayment)
//        cbNeedCourier = currentView!!.findViewById(R.id.cbNeedCourier)
        btnSave = currentView!!.findViewById(R.id.btnSave)

        //service cost region
        serviceCostView = View.inflate(context!!, R.layout.service_cost_add, null)
        ivBackServiceCost = serviceCostView!!.findViewById<ImageView>(R.id.ivBackServiceCost)
        etServiceCost = serviceCostView!!.findViewById<EditText>(R.id.etServiceCost)
        etCost = serviceCostView!!.findViewById<EditText>(R.id.etCost)
        btnSaveServiceCost = serviceCostView!!.findViewById<Button>(R.id.btnSaveServiceCost)
        ////////////////////////////////////


        tvAddServiceCost!!.setOnClickListener(this)
        tvAddTask!!.setOnClickListener(this)

        ivBack.setOnClickListener(this)
        btnSave.setOnClickListener(this)
        btnSaveServiceCost!!.setOnClickListener(this)
        ivBackServiceCost!!.setOnClickListener(this)
        ivCheck!!.setOnClickListener(this)

//        AnimateScroll.scrollToView(scroll, tvTicketName!!)
//        tvTicketName!!.requestFocus()

        if (editMode) {

            ivCheck!!.isEnabled = false
            ivCheck!!.setOnClickListener(null)
            ivCheck!!.visibility = View.GONE
            tvClientName!!.visibility = View.VISIBLE

            tvTicketDetails!!.text = getString(R.string.ticket_details)
            etMobile.isEnabled = false
            etMobile.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(12))
            btnSave.text = getString(R.string.update)
            tvTasks!!.visibility = View.VISIBLE
            tvAddTask!!.visibility = View.VISIBLE
            rvTasks!!.visibility = View.VISIBLE
            ivCheck!!.isEnabled = false
            getTicketById(AppConstants.CurrentSelectedTicket.TicketId!!)
        }


        refresh.setOnRefreshListener {
            if (editMode)
                getTicketById(AppConstants.CurrentSelectedTicket.TicketId!!)
            else
                refresh.isRefreshing = false
        }

    }

//    private fun prepareCategories(categoryList: ArrayList<TicketCategory>) {
//        dummyTicketCategoryList.clear()
//        if (categoryList.size > 0) {
//            var newArray = categoryList
//
//            var firstItem =
//                TicketCategory("", getString(R.string.select_category))
//            dummyTicketCategoryList.add(firstItem)
//
//            for (c in newArray.indices) {
//                dummyTicketCategoryList.add(newArray[c])
//            }
//        }
//
//
//        ticketCategoryAdapter = TicketCategoryAdapter(
//            context!!,
//            android.R.layout.simple_spinner_dropdown_item,
//            dummyTicketCategoryList
//        )
//        sCategory.setAdapter(ticketCategoryAdapter)
//        sCategory.isCursorVisible = false
//
//        sCategory.onItemClickListener =
//            AdapterView.OnItemClickListener { parent, _, position, _ ->
//                sCategory.showDropDown()
//                var category = parent.getItemAtPosition(position) as TicketCategory
//
//
//                if (!category.CategoryId.trim().isNullOrEmpty()) {
//                    selectedCategory = category
//                    sCategory.setText(category.Category)
//                } else {
//                    selectedCategory =
//                        TicketCategory("", getString(R.string.select_category))
//                    sCategory.setText(selectedCategory.Category)
//                }
//
//
//            }
//
//        sCategory.setOnClickListener(View.OnClickListener {
//            sCategory.showDropDown()
//
//        })
//
//
//    }

    private fun preparePriorities(prioritiesList: ArrayList<TicketPriority>) {
        dummyTicketPriorityList.clear()
        if (prioritiesList.size > 0) {
            var newArray = prioritiesList

            var firstItem = TicketPriority(null, getString(R.string.select_priority))
            dummyTicketPriorityList.add(firstItem)

            for (c in newArray.indices) {
                dummyTicketPriorityList.add(newArray[c])
            }
        }


        ticketPriorityAdapter = TicketPriorityAdapter(
            context!!,
            android.R.layout.simple_spinner_dropdown_item,
            dummyTicketPriorityList
        )
        sPriority.setAdapter(ticketPriorityAdapter)
        sPriority.isCursorVisible = false

        sPriority.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                sPriority.showDropDown()
                var priority = parent.getItemAtPosition(position) as TicketPriority


                if (priority.PriorityId != null) {
                    selectedPriority = priority
                    sPriority.setText(priority.Priority)
                } else {
                    selectedPriority =
                        TicketPriority(null, getString(R.string.select_priority))
                    sPriority.setText(selectedPriority.Priority)
                }


            }

        sPriority.setOnClickListener(View.OnClickListener {
            sPriority.showDropDown()

        })


    }

    private fun preparePaymentMethods(paymentMethodsList: ArrayList<TicketPaymentMethod>) {

        dummyTicketPaymentMethodList.clear()

        if (paymentMethodsList.size > 0) {
            var newArray = paymentMethodsList

            var firstItem =
                TicketPaymentMethod(null, getString(R.string.select_payment_method))
            dummyTicketPaymentMethodList.add(firstItem)

            for (c in newArray.indices) {
                dummyTicketPaymentMethodList.add(newArray[c])
            }
        }


        ticketPaymentMethodAdapter = TicketPaymentMethodAdapter(
            context!!,
            android.R.layout.simple_spinner_dropdown_item,
            dummyTicketPaymentMethodList
        )
        sPayment.setAdapter(ticketPaymentMethodAdapter)
        sPayment.isCursorVisible = false

        sPayment.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                sPayment.showDropDown()
                var paymentMethod = parent.getItemAtPosition(position) as TicketPaymentMethod


                if (paymentMethod.paymentId != null) {
                    selectedPaymentMethod = paymentMethod
                    sPayment.setText(paymentMethod.PaymentName)
                } else {
                    selectedPaymentMethod =
                        TicketPaymentMethod(
                            null,
                            getString(R.string.select_payment_method)
                        )
                    sPayment.setText(selectedPaymentMethod.PaymentName)
                }


            }

        sPayment.setOnClickListener(View.OnClickListener {
            sPayment.showDropDown()

        })


    }

    private fun prepareStatus(statusList: ArrayList<TicketStatus>) {

        dummyTicketStatusList.clear()

        if (statusList.size > 0) {
            var newArray = statusList

            var firstItem = TicketStatus(null, getString(R.string.select_status))
            dummyTicketStatusList.add(firstItem)

            for (c in newArray.indices) {
                dummyTicketStatusList.add(newArray[c])
            }
        }


        ticketStatusAdapter = TicketStatusAdapter(
            context!!,
            android.R.layout.simple_spinner_dropdown_item,
            dummyTicketStatusList
        )

        sStatus.setAdapter(ticketStatusAdapter)
        sStatus.isCursorVisible = false

        sStatus.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                sStatus.showDropDown()
                var ticketStatus = parent.getItemAtPosition(position) as TicketStatus


                if (ticketStatus.StatusId != null) {
                    selectedStatus = ticketStatus
                    sStatus.setText(selectedStatus.Status)
                } else {
                    selectedStatus =
                        TicketStatus(null, getString(R.string.select_status))
                    sStatus.setText(selectedStatus.Status)
                }


            }

        sStatus.setOnClickListener(View.OnClickListener {
            if (!ticketChangeToCompleteStatus(ticket.taskModel))
                sStatus.showDropDown()
            else
                Alert.showAlertMessage(
                    context!!,
                    AppConstants.WARNING,
                    "Can't change this ticket status because it has In Progress tasks."
                )

        })


    }

    fun ticketChangeToCompleteStatus(tasksList: ArrayList<Task>): Boolean {
        var prevent = false
        tasksList.forEach {
            if (it.Status == "In progress")
                prevent = true
            return prevent
        }
        return prevent
    }

    private fun prepareTicketSubData() {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getAllTicketSubDetails()
            NetworkManager().request(endPoint, object : INetworkCallBack<ApiResponse<data>> {
                override fun onFailed(error: String) {
                    Alert.hideProgress()
                    Alert.showMessage(
                        getString(R.string.error_login_server_error)
                    )
                }

                override fun onSuccess(response: ApiResponse<data>) {
                    if (response.Status == AppConstants.STATUS_SUCCESS) {
                        var subData = response.ResponseObj!!
                        if (subData != null) {
                            UserSessionManager.getInstance(context!!).setTicketSubData(subData)
//                            prepareCategories(subData.CategoriesModels!!)
                            prepareStatus(subData.StatusModels!!)
                            preparePriorities(subData.PrioritiesModels!!)
                            preparePaymentMethods(subData.paymentMethodModels!!)
                            Alert.hideProgress()
                        }


                    }
                    Alert.hideProgress()
                }
            })

        } else {
            Alert.hideProgress()
            Alert.showMessage(getString(R.string.no_internet))
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        }
        if (context is ITaskCallback) {
            taskListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
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
        if (index == 6)//go to task details vieadb w
            listener!!.onBottomSheetSelectedItem(6)
    }

    override fun onTaskDelete(task: Task?) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            if (task?.Status == AppConstants.NEW||task?.Status == AppConstants.WAITING) {
                var request = NetworkManager().create(ApiServices::class.java)
                var endPoint = request.getTaskDetails(task!!.TaskId)
                NetworkManager().request(
                    endPoint,
                    object : INetworkCallBack<ApiResponse<Task>> {
                        override fun onFailed(error: String) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                getString(R.string.error_login_server_error)
                            )
                        }

                        override fun onSuccess(response: ApiResponse<Task>) {
                            if (response.Status == AppConstants.STATUS_SUCCESS) {
                                taskInProgress = true
                                var task = response.ResponseObj!!
                                AppConstants.CurrentSelectedTask = task
                                Alert.hideProgress()

                                if (task.Status != AppConstants.IN_PROGRESS) {
                                    AlertDialog.Builder(context)
                                        .setTitle(AppConstants.WARNING)
                                        .setMessage(getString(R.string.message_delete) + " " + task.TaskName + " ?")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton(AppConstants.OK) { dialog, which ->
                                            deleteTask(task)
                                        }
                                        .setNegativeButton(AppConstants.CANCEL) { dialog, which -> }
                                        .show()
                                } else {
                                    getTicketById(AppConstants.CurrentSelectedTicket.TicketId!!)
                                    Alert.showAlertMessage(
                                        context!!,
                                        AppConstants.WARNING,
                                        "Can't edit this task it's in progress."
                                    )
                                }


                            } else {
                                Alert.hideProgress()
                                Alert.showMessage(
                                    getString(R.string.error_network)
                                )
                            }

                        }
                    })
            }
        } else {
            Alert.hideProgress()
            Alert.showMessage(
                getString(R.string.no_internet)
            )
        }


    }

    private fun getTaskDetails(taskId: String): Boolean {
        refresh.isRefreshing = false
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getTaskDetails(taskId)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<Task>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        refresh.isRefreshing = false
                        Alert.showMessage(
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Task>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            taskInProgress = true
                            var task = response.ResponseObj!!
                            AppConstants.CurrentSelectedTask = task
                            Alert.hideProgress()
                            refresh.isRefreshing = false
                        } else {
                            Alert.hideProgress()
                            refresh.isRefreshing = false
                            Alert.showMessage(
                                getString(R.string.error_network)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            refresh.isRefreshing = false
            Alert.showMessage(
                getString(R.string.no_internet)
            )
        }

        return taskInProgress
    }

    override fun onStopDelete(stop: Stop?) {

    }

    private fun loadTicketDetails(ticket: Ticket) {


        if (!ticket.UserName.trim().isNullOrEmpty()) {
            tvClientName!!.visibility = View.VISIBLE
            tvClientName?.text = ticket.UserName
        }

        AppConstants.TICKET_SERVICE_COST_LIST.clear()  //clear service cost data

        etTicketName.setText(ticket.TicketName)
        etTicketDescription.setText(ticket.TicketDescription)

        etMobile.setText(ticket.UserMobile)


        ccp?.fullNumber = ticket.UserMobile
//        ccp.isEnabled=false
        ccp?.setCcpClickable(false)


//        if (ticket.CategoryId != null) {
//            sCategory.setText(ticket.Category)
//            selectedCategory = TicketCategory(ticket.CategoryId!!, ticket.Category)
//        }
        if (ticket.PriorityId != null) {
            sPriority.setText(ticket.Priority)
            selectedPriority = TicketPriority(ticket.PriorityId!!, ticket.Priority!!)
        }
        if (ticket.StatusId != null) {
            sStatus.setText(ticket.Status)
            selectedStatus = TicketStatus(ticket.StatusId, ticket.Status!!)
        }
        if (ticket.PaymentMethodId != null) {
            sPayment.setText(ticket.PaymentMethod)
            selectedPaymentMethod =
                TicketPaymentMethod(ticket.PaymentMethodId!!, ticket.PaymentMethod!!)
        }


//        cbNeedCourier.isChecked = ticket.NeedCourier
        //ticket service cost
        if (ticket.serviceCosts != null && ticket.serviceCosts.size > 0) {
            rvServiceCost!!.visibility = View.VISIBLE
            AppConstants.TICKET_SERVICE_COST_LIST = ticket.serviceCosts
            prepareTicketServiceCost(ticket.serviceCosts)
        }
        if (ticket.taskModel.size > 0)
            rvTasks!!.visibility = View.VISIBLE
        loadTicketTasks(ticket.taskModel)

    }


    private fun loadTicketTasks(taskList: ArrayList<Task>) {

        var adapter = TaskAdapter(context!!, taskList, this, this)
        rvTasks!!.adapter = adapter
        rvTasks!!.layoutManager =
            LinearLayoutManager(AppController.getContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter.notifyDataSetChanged()
    }

    private fun deleteTask(task: Task) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.removeTask(task.TaskId, AppConstants.CurrentLoginAdmin.AdminId)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<Any?>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!.getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Any?>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
//                            var tasks = response.ResponseObj!!
                            var currentDeletedTask =
                                AppConstants.CurrentSelectedTicket.taskModel.find { it.TaskId == task.TaskId }
                            AppConstants.CurrentSelectedTicket.taskModel.remove(currentDeletedTask)

                            loadTicketTasks(AppConstants.CurrentSelectedTicket.taskModel)

                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                "Can't delete this task it's in progress."
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!.getString(R.string.error_login_server_error)
                            )
                        } else {
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!.getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(
                context!!.getString(R.string.no_internet)
            )
        }
    }


    private fun getTicketById(id: String): Ticket {
        ticket = Ticket()
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getTicketById(id)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<Ticket>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        refresh.isRefreshing = false
                        Alert.showMessage(
                            context!!.getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Ticket>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            ticket = response.ResponseObj!!
                            AppConstants.CurrentSelectedTicket = ticket
                            loadTicketDetails(ticket)
                            Alert.hideProgress()
                            refresh.isRefreshing = false
                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            refresh.isRefreshing = false
                            Alert.showMessage(
                                context!!.getString(R.string.error_login_server_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            refresh.isRefreshing = false
                            Alert.showMessage(
                                context!!.getString(R.string.error_login_server_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            refresh.isRefreshing = false
            Alert.showMessage(
                context!!.getString(R.string.no_internet)
            )
        }

        return ticket
    }


    private fun addTicket(ticketData: TicketModel) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.addTicket(ticketData)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<Ticket>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Ticket>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
//                            var tickets = response.ResponseObj!!
//                            var ticket = tickets[0]
//                            // add new task to the current ticket
//                            AppConstants.GetALLTicket.add(ticket)
                            listener!!.onBottomSheetSelectedItem(0)

                        } else if (response.Status == AppConstants.STATUS_NOT_EXIST) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                getString(R.string.error_mobile_error)
                            )
                        } else if (response.Status == AppConstants.STATUS_INCORRECT_DATA) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                getString(R.string.error_login_server_error)
                            )
                        } else {
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

    private fun getClientName(mobileNo: String) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getClientName(mobileNo)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<String?>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<String?>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
                            tvClientName?.visibility = View.VISIBLE
                            tvClientName?.text = response.ResponseObj
                        } else {
                            Alert.hideProgress()
                            Alert.showMessage(
                                getString(R.string.error_mobile_error)
                            )
                        }

                    }
                })

        } else {
            Alert.hideProgress()
            Alert.showMessage(getString(R.string.no_internet))
        }
    }

    private fun editTicket(ticketData: TicketModel) {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.editTicket(ticketData)
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<Ticket>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Ticket>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()

//                            var tickets = response.ResponseObj!!
//                            var ticket = tickets[0]
//
//                            AppConstants.CurrentSelectedTicket = ticket
                            listener!!.onBottomSheetSelectedItem(0)
                        } else if (response.Status == AppConstants.STATUS_FAILED) {
                            Alert.hideProgress()
                            Alert.showMessage(
                                response.Message
//                                getString(R.string.error_login_server_error)
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

    private fun validateAll(): Boolean {

        if (etTicketName.text.toString().isNullOrEmpty()) {
            Alert.showMessage("Ticket Name is required.")
            AnimateScroll.scrollToView(scroll, etTicketName)
            etTicketName.requestFocus()
            return false
        } else if (etTicketDescription.text.toString().isNullOrEmpty()) {
            Alert.showMessage("Ticket Description is required.")
            AnimateScroll.scrollToView(scroll, etTicketDescription)
            etTicketDescription.requestFocus()
            return false
        } else if (etMobile.text.trim().isNullOrEmpty() /*|| etMobile.text.length < 11*/) {
            Alert.showMessage("User Mobile is required.")
            AnimateScroll.scrollToView(scroll, etMobile)
            etMobile.requestFocus()
            return false
        } else if (!validatePhone(ccp!!, etMobile) && !editMode) {
            Alert.showMessage(getString(R.string.error_invalid_phone))
            AnimateScroll.scrollToView(scroll, etMobile)
            etMobile.requestFocus()
            return false
        }
//        else if (selectedCategory.CategoryId.isNullOrEmpty()) {
//            Alert. showMessage( "Category is required.")
//            AnimateScroll.scrollToView(scroll, etMobile)
//            sCategory.showDropDown()
//            return false
//        }
        else if (selectedStatus.StatusId == null) {
            Alert.showMessage("Status is required.")
            AnimateScroll.scrollToView(scroll, sStatus)
            sStatus.showDropDown()
            return false
        } else if (selectedPaymentMethod.paymentId == null) {
            Alert.showMessage("Payment is required.")
            AnimateScroll.scrollToView(scroll, sPayment)
            sPayment.showDropDown()
            return false
        }
//        else if (selectedPriority.PriorityId == null) {
//            Alert. showMessage( "Prirotiy is required.")
//            AnimateScroll.scrollToView(scroll, sPriority)
//            sPriority.showDropDown()
//            return false
//        }

        return true
    }

    private fun prepareTicketData() {
        var ticketId = AppConstants.CurrentSelectedTicket.TicketId
        var ticketName = etTicketName.text.toString()
        var ticketDescription = etTicketDescription.text.toString()
        var mobile = ccp?.fullNumber//"2" + etMobile.text.toString()


        var categoryId = "" //selectedCategory.CategoryId
        var priorityId = selectedPriority.PriorityId
        var statusId = selectedStatus.StatusId
        var paymentId = selectedPaymentMethod.paymentId
        var needCourier = true
        var adminId = AppConstants.CurrentLoginAdmin.AdminId


//        if (editMode)
//            ticketModel = TicketModel(
//                ticketId,
//                ticketName,
//                ticketDescription,
//                mobile,
//                categoryId,
//                statusId,
//                priorityId,
//                paymentId,
//                needCourier,
//                AppConstants.TICKET_SERVICE_COST_LIST,
//                adminId
//            )
//        else
        ticketModel = TicketModel(
            ticketId,
            ticketName,
            ticketDescription,
            mobile!!,
            categoryId,
            statusId,
            priorityId,
            paymentId,
            needCourier,
            AppConstants.TICKET_SERVICE_COST_LIST,
            adminId
        )


    }


    private fun prepareTicketServiceCost(serviceCostList: ArrayList<TicketServiceCost>) {
        if (ticketCategoryAdapter != null) {
            rvServiceCost!!.adapter = null
//            rvServiceCost!!.adapter!!.notifyDataSetChanged()
        }

        ticketServiceCostAdapter = TicketServiceCostAdapter(context!!, serviceCostList)
        rvServiceCost!!.adapter = ticketServiceCostAdapter
        rvServiceCost!!.layoutManager =
            LinearLayoutManager(AppController.getContext(), LinearLayoutManager.HORIZONTAL, false)
        ticketServiceCostAdapter!!.notifyDataSetChanged()


    }

    private fun showServiceCostWindow() {
        if (alertDialog == null) {
            var alert = AlertDialog.Builder(context!!)
            alertDialog = alert.create()

            etServiceCost!!.text.clear()
            etCost!!.text.clear()
            etServiceCost!!.requestFocus()
            alertDialog!!.setView(serviceCostView)

            alertDialog!!.show()
        } else {

            etServiceCost!!.text.clear()
            etCost!!.text.clear()
            etServiceCost!!.requestFocus()
            alertDialog!!.setView(serviceCostView)

            alertDialog!!.show()
        }


    }


    fun hideKeyboard(view: View) {
        val inputMethodManager =
            context!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager!!.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun validatePhone(mPhone: CountryCodePicker, mPhoneEdit: EditText): Boolean {
        mPhone.registerCarrierNumberEditText(mPhoneEdit)
        return mPhone.isValidFullNumber

    }

    override fun onDestroy() {
        super.onDestroy()
        Alert.hideProgress()
    }
}// Required empty public constructor
