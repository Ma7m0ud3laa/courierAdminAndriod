package com.kadabra.agent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kadabra.agent.R
import com.kadabra.agent.model.TicketServiceCost
import com.kadabra.agent.utilities.AppConstants

import java.util.ArrayList


/**
 * Created by Mokhtar on 1/8/2020.
 */

class TicketServiceCostAdapter(
    private val context: Context,
    private val serviceCostList: ArrayList<TicketServiceCost>
) :
    RecyclerView.Adapter<TicketServiceCostAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var serviceCost: TicketServiceCost = TicketServiceCost("", 0.0)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TicketServiceCostAdapter.MyViewHolder {
        val view = inflater.inflate(R.layout.service_cost_layout, parent, false)
        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: TicketServiceCostAdapter.MyViewHolder, position: Int) {
        serviceCost = serviceCostList[position]

        holder.tvTicketServiceCostName.text = serviceCost.serviceCostName
        holder.tvTicketServiceCostType.text = serviceCost.cost.toString()+ " " + context.getString(R.string.le)


    }

    override fun getItemCount(): Int {
        return serviceCostList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvTicketServiceCostName: TextView =
            itemView.findViewById(R.id.tvServiceCostName)
        var tvTicketServiceCostType: TextView = itemView.findViewById(R.id.tvCost)
        var tvDelete: TextView = itemView.findViewById(R.id.tvDelete)


        init {


            itemView.setOnClickListener {
                val pos = adapterPosition
                serviceCost = serviceCostList[pos]

            }




            tvDelete.setOnClickListener {
                val pos = adapterPosition
                AppConstants.TICKET_SERVICE_COST_LIST.removeAt(pos)
             notifyDataSetChanged()
            }

        }
    }


}
