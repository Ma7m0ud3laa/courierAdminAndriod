package com.twoam.agent.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.twoam.agent.model.Ticket

import java.util.ArrayList

/**
 * Created by Mokhtar on 6/25/2019.
 */

class TicketListAdapter(
    context: Context,
    resource: Int,
    private val values: ArrayList<Ticket>
) : ArrayAdapter<Ticket>(context, resource) {

    override fun getCount(): Int {
        return values.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getItem(position: Int): Ticket? {
        return values[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        val label = super.getView(position, convertView, parent) as TextView
        label.setTextColor(Color.BLACK)
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.text = values[position].TicketName
        label.tag=values[position].TicketId
        // And finally return your dynamic (or custom) view for each spinner item
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getDropDownView(position, convertView, parent) as TextView
        label.setTextColor(Color.BLACK)
        label.text = values[position].TicketName
        label.tag=values[position].TicketId

        return label
    }
}
