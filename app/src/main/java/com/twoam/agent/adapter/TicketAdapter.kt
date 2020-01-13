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
import com.twoam.agent.model.Ticket
import com.twoam.agent.utilities.AppConstants


/**
 * Created by Mokhtar on 1/8/2020.
 */

class TicketAdapter(
    private val context: Context,
    private val tasksList: ArrayList<Ticket>,
    private val _ticketListener: IBottomSheetCallback
) : RecyclerView.Adapter<TicketAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var ticket: Ticket = Ticket()
    private var listener: IBottomSheetCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketAdapter.MyViewHolder {

        val view = inflater.inflate(R.layout.task_layout, parent, false)
        listener = _ticketListener
        return MyViewHolder(view)

    }

    override fun onBindViewHolder(holder: TicketAdapter.MyViewHolder, position: Int) {
        ticket = tasksList[position]

        holder.tvUserName.text = ticket.title
        holder.tvTaskDetails.text = ticket.description
        holder.tvPickupLocation.text =
            ticket.pickUpLocation.lat.toString() + "," + ticket.pickUpLocation.lon.toString()
        holder.tvDropOffLocation.text =
            ticket.dropOffLocation.lat.toString() + "," + ticket.dropOffLocation.lon.toString()


    }

    override fun getItemCount(): Int {
        return tasksList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        var tvTaskDetails: TextView = itemView.findViewById(R.id.tvTaskDetails)
        var tvPickupLocation: TextView = itemView.findViewById(R.id.tvPickupLocation)
        var tvDropOffLocation: TextView = itemView.findViewById(R.id.tvDropOffLocation)


        init {


            itemView.setOnClickListener {
                val pos = adapterPosition
                ticket = tasksList[pos]
                AppConstants.currentSelectedTicket = ticket
                //switch to ticket details fragment
                AppConstants.currentSelectedTicket=ticket
                listener!!.onBottomSheetSelectedItem(3)

            }


        }


    }
}
