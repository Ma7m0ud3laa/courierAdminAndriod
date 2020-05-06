package com.kadabra.agent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kadabra.agent.R
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.model.Stop
import com.kadabra.agent.model.Task
import com.kadabra.agent.model.Ticket
import com.kadabra.agent.utilities.AppConstants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketAdapter.MyViewHolder {

        val view = inflater.inflate(R.layout.ticket_layout, parent, false)
        listener = _ticketListener
        return MyViewHolder(view)

    }

    override fun onBindViewHolder(holder: TicketAdapter.MyViewHolder, position: Int) {
        ticket = ticketList[position]
        taskList = ticket.taskModel

//        if (holder is MyViewHolder) {
//


        holder.tvStatus.text = ticket.Status
        holder.tvTicketName.text = ticket.TicketName
        if (!ticket.TicketDescription.isNullOrEmpty())
            holder.tvTicketDescription.text = ticket.TicketDescription

        holder.tvAgentName.text = ticket.AgentName
        holder.tvPriority.text = ticket.Priority


//        if (ticket.price!! > 0)
//            holder.tvComingTaskPickupTime.text = ticket.price.toString() + " " + context.getString(R.string.le)
//        else
//            holder.tvComingTaskPickupTime.text = "0 " + context.getString(R.string.le)


//        if (taskList.count() > 0) {
//            totalTasksAmount = 0.0
//            holder.tvTotalTasks.text =
//                taskList.count().toString()
//            taskList.forEach {
//                totalTasksAmount += it.Amount
//            }
//
//        }

        holder.tvTotalTasks.text = ticket.TotalTasks

//        if (totalTasksAmount > 0) {
//            holder.tvComingTaskPickupTime.text =
//                totalTasksAmount.toString() + " " + context.getString(R.string.le)
//        } else
//            holder.tvComingTaskPickupTime.text = "0 " + context.getString(R.string.le)


        if (!ticket.ComingTaskCourierName.isNullOrEmpty()) {
            holder.tvComingTaskCourierName.text =
                ticket.ComingTaskCourierName
        } else
            holder.tvComingTaskCourierName.visibility = View.INVISIBLE

        if (!ticket.ComingTaskPickupTime.isNullOrEmpty()) {
            var myFormat = "EEE, MMM d, yyyy   hh:mm a" //
            var t = "yyyy-MM-dd'T'HH:mm:ss"
            var sdf = SimpleDateFormat(t, Locale.US)
            var tf = SimpleDateFormat(myFormat, Locale.US)
            var dateObj = sdf.parse(ticket.ComingTaskPickupTime)
            holder.tvComingTaskPickupTime.text =
                tf.format(dateObj!!.time)
        } else
            holder.tvComingTaskPickupTime.visibility = View.INVISIBLE




        when (ticket.Status) {
            AppConstants.TaskStatus.NEW.status -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.link))
            }
            AppConstants.TaskStatus.IN_PROGRESS.status -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.crimson))
            }
            AppConstants.TaskStatus.POST_PONDED.status -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.black))
            }
            AppConstants.TaskStatus.COMPLETED.status -> {
                holder.tvStatus.setTextColor(context.getColor(R.color.green))
            }

        }

//        } else  {

//           var holder=LoadingViewHolder()
//            showLoadingView(holder, position)
//        }
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
        var tvAgentName: TextView = itemView.findViewById(R.id.tvAgentName)

        var tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        var tvComingTaskPickupTime: TextView = itemView.findViewById(R.id.tvComingTaskPickupTime)
        var tvComingTaskCourierName: TextView = itemView.findViewById(R.id.tvComingTaskCourierName)

        var tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        var tvTotalTasks: TextView = itemView.findViewById(R.id.tvTotalTasks)


        init {


            itemView.setOnClickListener {
                val pos = adapterPosition
                ticket = ticketList[pos]
//
//                if (checkPermissions()&& LocationHelper.shared.isGPSEnabled()) {
                AppConstants.CurrentSelectedTicket = ticket
                listener!!.onBottomSheetSelectedItem(3)
//                }
//                else
//                {
//                    if(!checkPermissions())
//                        Alert.showMessage(
//                            context,
//                            context.getString(R.string.permission_rationale)
//                        )
//                    else if(!LocationHelper.shared.isGPSEnabled())
//                        Alert.showMessage(
//                            context,
//                            context.getString(R.string.error_gps)
//                        )
//                }


            }


        }


    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

    }


    private fun showLoadingView(viewHolder: LoadingViewHolder, position: Int) {
        //ProgressBar would be displayed

    }


}
