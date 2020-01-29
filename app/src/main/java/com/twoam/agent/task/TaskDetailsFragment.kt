package com.twoam.agent.task


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.twoam.agent.R
import com.twoam.agent.adapter.StopAdapter
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.agent.callback.ITaskCallback
import com.twoam.agent.model.Stop
import com.twoam.agent.model.Task
import com.twoam.agent.utilities.AppConstants
import com.twoam.agent.utilities.AppController
import com.twoam.cartello.Utilities.Base.BaseFragment

/**
 * A simple [Fragment] subclass.
 */
class TaskDetailsFragment : BaseFragment(), IBottomSheetCallback, ITaskCallback {


    //region Members
    private var listener: IBottomSheetCallback? = null
    private var listenerTask: ITaskCallback? = null

    private var task: Task = Task()
    private var currentView: View? = null
    private var btnBack: ImageView? = null
    private var tvTask: TextView? = null
    private var tvTaskAmount: TextView? = null
    private var tvPickUp: TextView? = null
    private var tvDropOff: TextView? = null
    private var rvStops: RecyclerView? = null


    //endregion
    //region Events
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_task_details, container, false)
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
            listenerTask = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        listenerTask = null

    }
    //endregion
    //region Helper Functions

    private fun init() {
        btnBack = currentView?.findViewById(R.id.btnBack)
        tvTask = currentView?.findViewById(R.id.tvTask)
        tvTaskAmount = currentView?.findViewById(R.id.tvTaskAmount)
        tvPickUp = currentView?.findViewById(R.id.tvPickUp)
        tvDropOff = currentView?.findViewById(R.id.tvDropOff)
        rvStops = currentView?.findViewById(R.id.rvStops)

        loadTaskDetails()
    }

    private fun loadTaskDetails() {
        task = AppConstants.CurrentSelectedTask
        tvTask?.text = task.Task

        if (!task.Task.isNullOrEmpty())
            tvTask?.text = task.Task

        if (task.Amount!! > 0)
            tvTaskAmount?.text =
                task.Amount.toString() + " " + getString(R.string.le)

        if (task.stopsmodel.size > 0) {
            if (task.stopPickUp != null)
                tvPickUp?.text =
                    getString(R.string.pickup) + " " + task.stopPickUp.StopName
            if (task.stopDropOff != null)
                tvDropOff?.text =
                    getString(R.string.drop_off) + " " + task.stopDropOff.StopName

            if (task.defaultStops.size > 0)
                loadTaskStops(task.stopsmodel)

        } else {
            tvPickUp?.text =
                getString(R.string.pickup) + " " + getString(R.string.no_stop)
            tvDropOff?.text =
                getString(R.string.drop_off) + " " + getString(R.string.no_stop)
        }

    }

    private fun loadTaskStops(stopList: ArrayList<Stop>) {
        var adapter = StopAdapter(context!!, stopList, this)
        rvStops?.adapter = adapter
        rvStops?.layoutManager =
            LinearLayoutManager(AppController.getContext(), LinearLayoutManager.VERTICAL, false)
        adapter.notifyDataSetChanged()


    }
    //endregion


}
