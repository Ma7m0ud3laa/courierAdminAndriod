package com.twoam.agent.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.twoam.agent.R
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.agent.model.Stop
import com.twoam.agent.model.Task
import com.twoam.agent.utilities.AppConstants


/**
 * Created by Mokhtar on 1/8/2020.
 */

class TaskAdapter(
    private val context: Context,
    private val tasksList: ArrayList<Task>,
    private val _taskListener: IBottomSheetCallback
) :
    RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var task: Task = Task()
    private var listener: IBottomSheetCallback? = null
    private var stops = ArrayList<Stop>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.MyViewHolder {

        val view = inflater.inflate(R.layout.task_layout, parent, false)
        listener = _taskListener
        return MyViewHolder(view)

    }

    override fun onBindViewHolder(holder: TaskAdapter.MyViewHolder, position: Int) {
        task = tasksList[position]

        if (!task.Task.isNullOrEmpty())
            holder.tvTaskName.text = task.Task

        if (task.stopsmodel.size > 0) {
            task.stopsmodel.sortBy { it.StopTypeID }
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


        if (task.Amount!! > 0)
            holder.tvTaskAmount.text = task.Amount.toString() + " " + context.getString(R.string.le)
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
        var tvPickupLocation: TextView = itemView.findViewById(R.id.tvPickupLocation)
        var tvDropOffLocation: TextView = itemView.findViewById(R.id.tvDropOffLocation)


        init {

            itemView.setOnClickListener {
                val pos = adapterPosition
                task = tasksList[pos]
                AppConstants.CurrentSelectedTask = task
                listener!!.onBottomSheetSelectedItem(6)//go to task details view

            }


        }


    }
}
