package com.twoam.agent.ticket

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoam.Networking.INetworkCallBack
import com.twoam.Networking.NetworkManager
import com.twoam.agent.R
import com.twoam.agent.adapter.TaskAdapter
import com.twoam.agent.api.ApiResponse
import com.twoam.agent.api.ApiServices
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.agent.callback.ITaskCallback
import com.twoam.agent.model.Stop
import com.twoam.agent.model.Task
import com.twoam.agent.model.Ticket
import com.twoam.agent.utilities.Alert
import com.twoam.agent.utilities.AnimateScroll
import com.twoam.agent.utilities.AppConstants
import com.twoam.agent.utilities.AppController
import com.twoam.cartello.Utilities.Base.BaseFragment


class TicketDetailsFragment : BaseFragment(), IBottomSheetCallback, ITaskCallback,
    View.OnClickListener {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private lateinit var scroll: ScrollView
    private var tvTicketName: TextView? = null
    private var tvTicketDetails: TextView? = null
    private var tvStatus: TextView? = null
    private var tvPriority: TextView? = null
    private var tvPrice: TextView? = null
    private var tvAddTask: TextView? = null
    private var rvTasks: RecyclerView? = null
    private var currentView: View? = null
    private var ivBack: ImageView? = null
    private var listener: IBottomSheetCallback? = null
    private var taskListener: ITaskCallback? = null
    private var ticket = Ticket()
    var editMode = false

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
        currentView = inflater.inflate(R.layout.fragment_ticket_details, container, false)
        init()

        if (!ticket.TicketId.isNullOrEmpty()) {
            loadTicketDetails()
        }
        return currentView
    }


    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.tvAddTask -> {// add task to the current ticket
                listener!!.onBottomSheetSelectedItem(7)
            }
            R.id.ivBack -> {
                listener!!.onBottomSheetSelectedItem(0)
            }
        }

    }


    private fun init() {
        scroll = currentView!!.findViewById(R.id.scroll)
        tvTicketName = currentView!!.findViewById(R.id.tvTicketName)
        tvTicketDetails = currentView!!.findViewById(R.id.tvTicketDetails)
        tvStatus = currentView!!.findViewById(R.id.tvStatus)
        tvPriority = currentView!!.findViewById(R.id.tvPriority)
        tvPrice = currentView!!.findViewById(R.id.tvPrice)
        tvAddTask = currentView!!.findViewById(R.id.tvAddTask)
        ivBack = currentView!!.findViewById(R.id.ivBack)
        rvTasks = currentView!!.findViewById(R.id.rvTasks)

        tvAddTask!!.setOnClickListener(this)
        ivBack!!.setOnClickListener(this)

        AnimateScroll.scrollToView(scroll, tvTicketName!!)
        tvTicketName!!.requestFocus()

        ticket = AppConstants.CurrentSelectedTicket


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

        var ticket = AppConstants.CurrentSelectedTicket
        tvTicketName!!.text = ticket.TicketName

        if (!ticket.TicketDescription.isNullOrEmpty())
            tvTicketDetails!!.text = ticket.TicketDescription

        tvStatus!!.text = ticket.Status
        tvPriority!!.text = ticket.Priority

//        if (ticket.price!! > 0)
//            tvPrice!!.text = ticket.price.toString()
//        else
//            tvPrice!!.text = "0 " + context!!.getString(R.string.le)


        var totalTasksAmount = 0.0

        ticket.taskModel.forEach {
            totalTasksAmount += it.Amount
        }

        if (totalTasksAmount > 0)
            tvPrice!!.text =
                totalTasksAmount.toString() + " " + context!!.getString(R.string.le)
        else
            tvPrice!!.text = "0 " + context!!.getString(R.string.le)



        when (ticket.Status) {
            AppConstants.TaskStatus.NEW.status -> {
                tvStatus!!.setTextColor(context!!.getColor(R.color.link))
            }
            AppConstants.TaskStatus.IN_PROGRESS.status -> {
                tvStatus!!.setTextColor(context!!.getColor(R.color.crimson))
            }
            AppConstants.TaskStatus.POST_PONDED.status -> {
                tvStatus!!.setTextColor(context!!.getColor(R.color.greenYellow))
            }
            AppConstants.TaskStatus.COMPLETED.status -> {
                tvStatus!!.setTextColor(context!!.getColor(R.color.green))
            }

        }

        loadTicketTasks(ticket.taskModel!!)
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
            Alert.showMessage(context!!, context!!.getString(R.string.no_internet))
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
            Alert.showMessage(context!!, context!!.getString(R.string.no_internet))
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
         * @return A new instance of fragment TicketDetailsFragment.
         */
//        // TODO: Rename and change types and number of parameters
//        fun newInstance(param1: String, param2: String): TicketDetailsFragment {
//            val fragment = TicketDetailsFragment()
//            val args = Bundle()
//            args.putString(ARG_PARAM1, param1)
//            args.putString(ARG_PARAM2, param2)
//            fragment.arguments = args
//            return fragment
//        }
    }
}// Required empty public constructor
