package com.kadabra.agent.ticket

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
import com.kadabra.cartello.Utilities.Base.BaseFragment
import com.reach.plus.admin.util.UserSessionManager
import android.widget.TextView
import android.widget.EditText


class NewTicketFragment : BaseFragment(), IBottomSheetCallback, ITaskCallback,
    View.OnClickListener {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private lateinit var scroll: ScrollView
    private var tvTicketName: TextView? = null
    private var tvTicketDetails: TextView? = null
    private var tvStatus: TextView? = null
    private var tvPriority: TextView? = null
    private var tvPrice: TextView? = null
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
    private lateinit var sCategory: AutoCompleteTextView
    private lateinit var sPriority: AutoCompleteTextView
    private lateinit var sStatus: AutoCompleteTextView
    private lateinit var sPayment: AutoCompleteTextView
    private lateinit var cbNeedCourier: CheckBox
    private lateinit var ivBack: ImageView
    private lateinit var btnSave: Button
    private var listener: IBottomSheetCallback? = null
    private var taskListener: ITaskCallback? = null
    private var ticketModel = TicketModel()
    private var ticket = Ticket()

    private var dummyTicketCategoryList = ArrayList<TicketCategory>()
    private var dummyTicketStatusList = ArrayList<TicketStatus>()
    private var dummyTicketPriorityList = ArrayList<TicketPriority>()
    private var dummyTicketPaymentMethodList = ArrayList<TicketPaymentMethod>()
    private var selectedCategory = TicketCategory("", "")
    private var selectedStatus = TicketStatus(0, "")
    private var selectedPriority = TicketPriority(0, "")
    private var selectedPaymentMethod = TicketPaymentMethod(0, "")
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
    var actionState = 1 //1 add 2 edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }

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
//            if (subData != null) {
//                prepareCategories(subData.CategoriesModels!!)
//                prepareStatus(subData.StatusModels!!)
//                preparePriorities(subData.PrioritiesModels!!)
//                preparePaymentMethods(subData.paymentMethodModels!!)
//            } else {
            prepareTicketSubData()
//            }
        } else
            Alert.showMessage(context!!, getString(R.string.no_internet))



        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AppConstants.CurrentSelectedTicket.TicketId.isNullOrEmpty()) {
            loadTicketDetails()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.tvAddServiceCost -> {// add task to the current ticket
                if (NetworkManager().isNetworkAvailable(context!!))
                    showServiceCostWindow()
                else
                    Alert.showMessage(context!!, getString(R.string.no_internet))
            }

            R.id.tvAddTask -> {// add task to the current ticket
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
                            AppConstants.TICKET_SERVICE_COST_LIST.add(serviceCost!!)
                            prepareTicketServiceCost(AppConstants.TICKET_SERVICE_COST_LIST)
                            alertDialog!!.dismiss()

                        }
                    }
                } else
                    Alert.showMessage(context!!, getString(R.string.no_internet))

            }
            R.id.btnSave -> {
                if (NetworkManager().isNetworkAvailable(context!!)) {
                    if (validateAll()) {
                        prepareTicketData()
                        if (!editMode)
                            addTicket(ticketModel)
                        else
                            editTicket(ticketModel)
                    }
                } else
                    Alert.showMessage(context!!, getString(R.string.no_internet))

            }
        }

    }

    private fun validateServiceCost(): Boolean {

        if (etServiceCost!!.text.trim().toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Service Cost Name is required.")
            etServiceCost!!.requestFocus()
            return false
        }
        if (etCost!!.text.trim().toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Cost is required.")
            etCost!!.requestFocus()
            return false
        }
        return true
    }


    private fun init() {
        scroll = currentView!!.findViewById(R.id.scroll)
        tvTicketName = currentView!!.findViewById(R.id.tvTicketDetails)
        tvTicketDetails = currentView!!.findViewById(R.id.tvTicketDetails)
        tvStatus = currentView!!.findViewById(R.id.tvStatus)
        tvPriority = currentView!!.findViewById(R.id.tvPriority)
        tvPrice = currentView!!.findViewById(R.id.tvPrice)
        tvAddServiceCost = currentView!!.findViewById(R.id.tvAddServiceCost)
        tvAddTask = currentView!!.findViewById(R.id.tvAddTask)
        tvTasks = currentView!!.findViewById(R.id.tvTasks)

        ivBack = currentView!!.findViewById(R.id.ivBack)
        rvServiceCost = currentView!!.findViewById(R.id.rvServiceCost)
        rvTasks = currentView!!.findViewById(R.id.rvTasks)
        etTicketName = currentView!!.findViewById(R.id.etTicketName)
        etTicketDescription = currentView!!.findViewById(R.id.etTicketDescription)
        etMobile = currentView!!.findViewById(R.id.etMobile)
        sCategory = currentView!!.findViewById(R.id.sCategory)
        sPriority = currentView!!.findViewById(R.id.sPriority)
        sStatus = currentView!!.findViewById(R.id.sStatus)
        sPayment = currentView!!.findViewById(R.id.sPayment)
        cbNeedCourier = currentView!!.findViewById(R.id.cbNeedCourier)
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

        ivBack!!.setOnClickListener(this)
        btnSave!!.setOnClickListener(this)
        btnSaveServiceCost!!.setOnClickListener(this)
        ivBackServiceCost!!.setOnClickListener(this)

        AnimateScroll.scrollToView(scroll, tvTicketName!!)
        tvTicketName!!.requestFocus()


    }

    private fun prepareCategories(categoryList: ArrayList<TicketCategory>) {
        dummyTicketCategoryList.clear()
        if (categoryList.size > 0) {
            var newArray = categoryList

            var firstItem =
                TicketCategory("", getString(R.string.select_category))
            dummyTicketCategoryList.add(firstItem)

            for (c in newArray.indices) {
                dummyTicketCategoryList.add(newArray[c])
            }
        }


        ticketCategoryAdapter = TicketCategoryAdapter(
            context!!,
            android.R.layout.simple_spinner_dropdown_item,
            dummyTicketCategoryList
        )
        sCategory.setAdapter(ticketCategoryAdapter)
        sCategory.isCursorVisible = false

        sCategory.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                sCategory.showDropDown()
                var category = parent.getItemAtPosition(position) as TicketCategory


                if (!category.CategoryId.trim().isNullOrEmpty()) {
                    selectedCategory = category
                    sCategory.setText(category.Category)
                } else {
                    selectedCategory =
                        TicketCategory("", getString(R.string.select_category))
                    sCategory.setText(selectedCategory.Category)
                }


            }

        sCategory.setOnClickListener(View.OnClickListener {
            sCategory.showDropDown()

        })


    }

    private fun preparePriorities(prioritiesList: ArrayList<TicketPriority>) {
        dummyTicketPriorityList.clear()
        if (prioritiesList.size > 0) {
            var newArray = prioritiesList

            var firstItem = TicketPriority(0, getString(R.string.select_priority))
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
            AdapterView.OnItemClickListener { parent, view, position, id ->
                sPriority.showDropDown()
                var priority = parent.getItemAtPosition(position) as TicketPriority


                if (priority.PriorityId > 0) {
                    selectedPriority = priority
                    sPriority.setText(priority.Priority)
                } else {
                    selectedPriority =
                        TicketPriority(0, getString(R.string.select_priority))
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
                TicketPaymentMethod(0, getString(R.string.select_payment_method))
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


                if (paymentMethod.paymentId > 0) {
                    selectedPaymentMethod = paymentMethod
                    sPayment.setText(paymentMethod.PaymentName)
                } else {
                    selectedPaymentMethod =
                        TicketPaymentMethod(
                            0,
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

            var firstItem = TicketStatus(0, getString(R.string.select_status))
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


                if (ticketStatus.StatusId > 0) {
                    selectedStatus = ticketStatus
                    sStatus.setText(selectedStatus.Status)
                } else {
                    selectedStatus =
                        TicketStatus(0, getString(R.string.select_status))
                    sStatus.setText(selectedStatus.Status)
                }


            }

        sStatus.setOnClickListener(View.OnClickListener {
            sStatus.showDropDown()

        })


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
                        context!!,
                        getString(R.string.error_login_server_error)
                    )
                }

                override fun onSuccess(response: ApiResponse<data>) {
                    if (response.Status == AppConstants.STATUS_SUCCESS) {
                        var subData = response.ResponseObj!!
                        if (subData != null) {
                            UserSessionManager.getInstance(context!!).setTicketSubData(subData)
                            prepareCategories(subData.CategoriesModels!!)
                            prepareStatus(subData.StatusModels!!)
                            preparePriorities(subData.PrioritiesModels!!)
                            preparePaymentMethods(subData.paymentMethodModels!!)
                            Alert.hideProgress()
                        }


                    }

                }
            })

        } else {
            Alert.hideProgress()
            Alert.showMessage(context!!, getString(R.string.no_internet))
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
        if (index == 6)//go to task details view
            listener!!.onBottomSheetSelectedItem(6)
    }

    override fun onTaskDelete(task: Task?) {
        if (!task!!.TaskId.isNullOrEmpty()) {
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

    override fun onStopDelete(stop: Stop?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun loadTicketDetails() {

        tvTasks!!.visibility = View.VISIBLE
        tvAddTask!!.visibility = View.VISIBLE
        rvTasks!!.visibility = View.VISIBLE
        tvTicketDetails!!.text = getString(R.string.ticket_details)
        etMobile.isEnabled = false
        btnSave.text = getString(R.string.update)


        var ticket = AppConstants.CurrentSelectedTicket
        etTicketName.setText(ticket.TicketName)
        etTicketDescription.setText(ticket.TicketDescription)
        etMobile.setText(ticket.UserMobile)


        if (!ticket.CategoryId.trim().isNullOrEmpty()) {
            sCategory.setText(ticket.Category)
            selectedCategory = TicketCategory(ticket.CategoryId, ticket.Category)
        }
        if (ticket.PriorityId > 0) {
            sPriority.setText(ticket.Priority)
            selectedPriority = TicketPriority(ticket.PriorityId, ticket.Priority)
        }
        if (ticket.statusId > 0) {
            sStatus.setText(ticket.Status)
            selectedStatus = TicketStatus(ticket.statusId, ticket.Status)
        }
        if (ticket.PaymentId > 0) {
            sPayment.setText(ticket.PaymentName)
            selectedPaymentMethod =
                TicketPaymentMethod(ticket.PaymentId, ticket.PaymentName)
        }
        if (ticket.NeedCourier)
            cbNeedCourier.isChecked = true

        cbNeedCourier.isChecked = ticket.NeedCourier
        //ticket service cost
        if (ticket.serviceCosts != null && ticket.serviceCosts!!.size > 0) {
            prepareTicketServiceCost(ticket.serviceCosts!!)
        }
        if (ticket.taskModel.size > 0)
            loadTicketTasks(ticket.taskModel)

    }


    private fun loadTicketTasks(taskList: ArrayList<Task>) {

        var adapter = TaskAdapter(context!!, taskList, this, this)
        rvTasks!!.adapter = adapter
        rvTasks!!.layoutManager =
            LinearLayoutManager(AppController.getContext(), LinearLayoutManager.VERTICAL, false)
        adapter.notifyDataSetChanged()
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
                            loadTicketTasks(tasks)

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
            Alert.showMessage(
                context!!,
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
                        Alert.showMessage(
                            context!!,
                            context!!.getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<Ticket>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
                            ticket = response.ResponseObj!!
                            AppConstants.CurrentSelectedTicket = ticket


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
            Alert.showMessage(
                context!!,
                context!!.getString(R.string.no_internet)
            )
        }

        return ticket
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewTicketFragment.
         */
//        // TODO: Rename and change types and number of parameters
//        fun newInstance(param1: String, param2: String): NewTicketFragment {
//            val fragment = NewTicketFragment()
//            val args = Bundle()
//            args.putString(ARG_PARAM1, param1)
//            args.putString(ARG_PARAM2, param2)
//            fragment.arguments = args
//            return fragment
//        }
    }

    private fun addTicket(ticketData: TicketModel) {

        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            actionState = 1
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.addTicket(
                ticketData, actionState
            )
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Ticket>>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Ticket>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()
                            var tickets = response.ResponseObj!!
                            var ticket = tickets[0]
                            // add new task to the current ticket
                            AppConstants.GetALLTicket.add(ticket)
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
                        } else {
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

    private fun editTicket(ticketData: TicketModel) {

        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            actionState = 2
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.addTicket(
                ticketData, actionState
            )
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Ticket>>> {
                    override fun onFailed(error: String) {
                        Alert.hideProgress()
                        Alert.showMessage(
                            context!!,
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Ticket>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Alert.hideProgress()

                            var tickets = response.ResponseObj!!
                            var ticket = tickets[0]

                            AppConstants.CurrentSelectedTicket = ticket
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

    private fun validateAll(): Boolean {

        if (etTicketName.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Ticket Name is required.")
            AnimateScroll.scrollToView(scroll, etTicketName)
            etTicketName.requestFocus()
            return false
        } else if (etTicketDescription.text.toString().isNullOrEmpty()) {
            Alert.showMessage(context!!, "Ticket Description is required.")
            AnimateScroll.scrollToView(scroll, etTicketDescription)
            etTicketDescription.requestFocus()
            return false
        } else if (etMobile.text.trim().isNullOrEmpty() || etMobile.text.length < 11) {
            Alert.showMessage(context!!, "User Mobile is required,and must be 11 digit.")
            AnimateScroll.scrollToView(scroll, etMobile)
            etMobile.requestFocus()
            return false
        } else if (selectedCategory.CategoryId == "") {
            Alert.showMessage(context!!, "Category is required.")
            AnimateScroll.scrollToView(scroll, etMobile)
            sCategory.showDropDown()
            return false
        }

        return true
    }

    private fun prepareTicketData() {

        var ticketName = etTicketName.text.toString()
        var ticketDescription = etTicketDescription.text.toString()
        var mobile = "2" + etMobile.text.toString()
        var categoryId = selectedCategory.CategoryId
        var priorityId = selectedPriority.PriorityId
        var statusId = selectedStatus.StatusId
        var paymentId = selectedPaymentMethod.paymentId
        var needCourier = cbNeedCourier.isChecked
        var adminId = AppConstants.CurrentLoginAdmin.AdminId

        ticketModel = TicketModel(
            AppConstants.CurrentSelectedTicket.TicketId,
            ticketName,
            ticketDescription,
            mobile,
            categoryId,
            statusId,
            priorityId,
            paymentId,
            needCourier,
            AppConstants.TICKET_SERVICE_COST_LIST!!,
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
            alertDialog!!.setView(serviceCostView)
            etServiceCost!!.text.clear()
            etCost!!.text.clear()


        } else {

            etServiceCost!!.text.clear()
            etCost!!.text.clear()
            alertDialog!!.setView(serviceCostView)
            alertDialog!!.show()
        }

        alertDialog!!.show()

    }


}// Required empty public constructor
