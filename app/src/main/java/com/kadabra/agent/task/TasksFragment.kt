package com.kadabra.agent.task


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kadabra.Utilities.Base.BaseFragment
import com.kadabra.agent.R
import com.kadabra.agent.adapter.TaskAdapter
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.callback.ITaskCallback
import com.kadabra.agent.model.Stop
import com.kadabra.agent.model.Task
import com.kadabra.agent.model.Ticket
import com.kadabra.agent.utilities.AppConstants
import com.kadabra.agent.utilities.AppController


/**
 * A simple [Fragment] subclass.
 */
class TasksFragment : BaseFragment(), IBottomSheetCallback, ITaskCallback {

    //region Members
    private var ticket: Ticket = Ticket()
    private var tasksList = ArrayList<Task>()
    private var rvTasks: RecyclerView? = null
    private var listener: IBottomSheetCallback? = null
    private var taskListener: ITaskCallback? = null
    private var sRefresh: SwipeRefreshLayout? = null
    private var ivNoInternet: ImageView? = null
    private var currentView: View? = null

    //endregion
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_tasks, container, false)
        init()
        return currentView
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBottomSheetSelectedItem(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTaskDelete(task: Task?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStopDelete(stop: Stop?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        }
        if (context is ITaskCallback) {
            taskListener = context
        }

        else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
    //region Events

    //endregion
    //region Helper Functions

    private fun init() {
        rvTasks = currentView?.findViewById(R.id.rvTasks)
        sRefresh = currentView?.findViewById(R.id.sRefresh)
        ivNoInternet = currentView?.findViewById(R.id.ivNoInternet)

        sRefresh?.setOnRefreshListener { loadTasks(tasksList) }

        ticket = AppConstants.CurrentSelectedTicket
        tasksList = ticket.taskModel

        loadTasks(tasksList)
    }


    private fun loadTasks(tasksList: ArrayList<Task>) {
        if (tasksList.size > 0) {
            var adapter = TaskAdapter(context!!, tasksList, listener!!,taskListener!!)
            rvTasks!!.adapter = adapter
            rvTasks?.layoutManager =
                GridLayoutManager(
                    AppController.getContext(),
                    1,
                    GridLayoutManager.VERTICAL,
                    false
                )
        }

    }

//endregion
}





