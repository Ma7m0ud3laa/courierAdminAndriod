package com.kadabra.agent.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kadabra.agent.R
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.callback.ITaskCallback
import com.kadabra.agent.model.Stop
import com.kadabra.agent.model.Task
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AppConstants


/**
 * Created by Mokhtar on 1/8/2020.
 */

class TaskAdapter(
    private val context: Context,
    private val tasksList: ArrayList<Task>,
    private val _taskListener: IBottomSheetCallback,
    private val _deleteTaskListener: ITaskCallback

) :
    RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var task: Task = Task()
    private var listener: IBottomSheetCallback? = null
    private var deleteTaskListener: ITaskCallback? = null


    private var stops = ArrayList<Stop>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.MyViewHolder {

        val view = inflater.inflate(R.layout.task_layout, parent, false)
        listener = _taskListener
        deleteTaskListener = _deleteTaskListener
        return MyViewHolder(view)

    }

    override fun onBindViewHolder(holder: TaskAdapter.MyViewHolder, position: Int) {
        task = tasksList[position]

        if (!task.TaskName.isNullOrEmpty())
            holder.tvTaskName.text = task.TaskName

        holder.tvStatus.text = task.Status

        if (task.stopsmodel.size > 0) {
//            task.stopsmodel.sortBy { it.StopTypeID }
            task.stopsmodel.forEach {

                when (it.StopTypeID) {
                    1 -> { //pickup
                        task.stopPickUp = it
                        holder.tvPickupLocation.text =
                            context.getString(R.string.from) + " " + it.StopName

                    }
                    2 -> { //dropOff
                        task.stopDropOff = it
                        holder.tvDropOffLocation.text =
                            context.getString(R.string.to) + " " + it.StopName
                    }
                    3 ->
                        task.defaultStops.add(it)


                }

            }

        } else {
            holder.tvPickupLocation.text =
                context.getString(R.string.from) + " " + context.getString(R.string.no_stop)
            holder.tvDropOffLocation.text =
                context.getString(R.string.to) + " " + context.getString(R.string.no_stop)
        }


        if (task.Amount > 0)
            holder.tvTaskAmount.text =
                task.Amount.toString() + " " + context.getString(R.string.le)
        else
            holder.tvTaskAmount.text = "0 " + context.getString(R.string.le)


    }

    override fun getItemCount(): Int {
        return tasksList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        var tvTaskAmount: TextView = itemView.findViewById(R.id.tvTaskAmount)
        var tvPickupLocation: TextView =
            itemView.findViewById(R.id.tvPickupLocation)
        var tvDropOffLocation: TextView =
            itemView.findViewById(R.id.tvDropOffLocation)
        var tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
        var tvStatus: TextView = itemView.findViewById(R.id.tvStatus)


        init {

            itemView.setOnClickListener {
                val pos = adapterPosition
                task = tasksList[pos]
                AppConstants.CurrentSelectedTask = task
                listener!!.onBottomSheetSelectedItem(6)//go to task details view

            }

            itemView.setOnLongClickListener {
                val pos = adapterPosition
                task = tasksList[pos]
                deleteTaskListener!!.onTaskDelete(task)
//                var anim = AnimationUtils.loadAnimation(context, R.anim.shake)
//                itemView.startAnimation(anim)
                false
            }


            tvDelete.setOnClickListener {
                //delete the current task
                val pos = adapterPosition
                task = tasksList[pos]

//                if (task.Status == AppConstants.NEW||task.Status == AppConstants.WAITING) {
                if (task.Status == AppConstants.NEW) {
                    Log.d("TAG", task.Status)
                    deleteTaskListener!!.onTaskDelete(task)
                } else {
                    Alert.showAlertMessage(
                        context,
                        AppConstants.WARNING,
                        "Can't delete this task."
                    )
                }
//                var anim = AnimationUtils.loadAnimation(context, R.anim.shake)
//                itemView.startAnimation(anim)
            }


        }


    }

}
