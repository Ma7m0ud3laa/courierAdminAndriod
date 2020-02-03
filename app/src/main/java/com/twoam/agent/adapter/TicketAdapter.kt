package com.twoam.agent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.twoam.agent.R
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.agent.model.Stop
import com.twoam.agent.model.Task
import com.twoam.agent.model.Ticket
import com.twoam.agent.utilities.AppConstants


/**
 * Created by Mokhtar on 1/8/2020.
 */

class TicketAdapter(
    private val context: Context,
    private val ticketList: ArrayList<Ticket>,
    private val _ticketListener: IBottomSheetCallback

) : RecyclerView.Adapter<TicketAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var ticket: Ticket = Ticket()
    private var taskList = ArrayList<Task>()
    private var taskStops = ArrayList<Stop>()
    private var listener: IBottomSheetCallback? = null
    private var totalTasksAmount = 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketAdapter.MyViewHolder {

        val view = inflater.inflate(R.layout.ticket_layout, parent, false)
        listener = _ticketListener
        return MyViewHolder(view)

    }

    override fun onBindViewHolder(holder: TicketAdapter.MyViewHolder, position: Int) {
        ticket = ticketList[position]
        taskList = ticket.taskModel

        holder.tvStatus.text = ticket.Status
        holder.tvTicketName.text = ticket.TicketName
        if (!ticket.TicketDescription.isNullOrEmpty())
            holder.tvTicketDescription.text = ticket.TicketDescription
        holder.tvPriority.text = ticket.Priority


//        if (ticket.price!! > 0)
//            holder.tvPrice.text = ticket.price.toString() + " " + context.getString(R.string.le)
//        else
//            holder.tvPrice.text = "0 " + context.getString(R.string.le)


        if (taskList.count() > 0) {
            totalTasksAmount = 0.0
            holder.tvTotalTasks.text =
                taskList.count().toString()
            taskList.forEach {
                totalTasksAmount += it.Amount
            }


        }

        if (totalTasksAmount > 0) {
            holder.tvPrice.text =
                totalTasksAmount.toString() + " " + context.getString(R.string.le)
        }

        else
            holder.tvPrice.text = "0 " + context.getString(R.string.le)


        when (ticket.Status) {
            AppConstants.TaskStatus.NEW.status -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.link))
            }
            AppConstants.TaskStatus.IN_PROGRESS.status -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.crimson))
            }
            AppConstants.TaskStatus.POST_PONDED.status -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.greenYellow))
            }
            AppConstants.TaskStatus.COMPLETED.status -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.green))
            }

        }
    }

    override fun getItemCount(): Int {
        return ticketList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvTicketName: TextView = itemView.findViewById(R.id.tvTicketName)
        var tvTicketDescription: TextView = itemView.findViewById(R.id.tvTicketDescription)
        var tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        var tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        var tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        var tvTotalTasks: TextView = itemView.findViewById(R.id.tvTotalTasks)


        init {


            itemView.setOnClickListener {
                val pos = adapterPosition
                ticket = ticketList[pos]
                AppConstants.CurrentSelectedTicket = ticket
                listener!!.onBottomSheetSelectedItem(3)

            }


        }


    }

    enum class StatusType(var status: String) {
        NEW("New"),
        POST_PONDED("FEB"),
        IN_PROGRESS("In progress"),
        DIS_PAUSED("In progress");
    }


}
