package com.kadabra.agent.adapter

import android.content.Context
import android.graphics.Color
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.kadabra.agent.R

import com.kadabra.agent.model.Courier
import com.kofigyan.stateprogressbar.StateProgressBar
import org.w3c.dom.Text

import java.util.ArrayList

/**
 * Created by Mokhtar on 6/25/2019.
 */

class CourierListAdapter(
    context: Context,
    resource: Int,
    private val values: ArrayList<Courier>
) : ArrayAdapter<Courier>(context, resource) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var ivCourierImage: ImageView? = null
    private var tvName: TextView? = null
    private var tvMobile: TextView? = null
    private var tvHaveTaskNow: TextView? = null
    private var progress: StateProgressBar? = null



    override fun getCount(): Int {
        return values.size
    }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }


        override fun getItem(position: Int): Courier? {
            return values[position]
        }

    override fun getView(position: Int,  convertView: View?, parent: ViewGroup): View {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item

        if (convertView == null) {
            var  view = inflater.inflate(R.layout.courier_layout, parent, false)

            ivCourierImage = convertView?.findViewById(R.id.ivCourierImage)
            tvName = convertView?.findViewById(R.id.tvName)
            tvMobile = convertView?.findViewById(R.id.tvMobile)
            tvHaveTaskNow = convertView?.findViewById(R.id.tvHaveTaskNow)
            progress = convertView?.findViewById(R.id.progress)

        }

//        val label = super.getView(position, convertView, parent) as TextView
//        label.setTextColor(Color.BLACK)
//        // Then you can get the current item using the values array (Users array) and the current position
//        // You can NOW reference each method you has created in your bean object (User class)
//        label.text = values[position].CourierName
//        // And finally return your dynamic (or custom) view for each spinner item
//
//        return label

        return convertView!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val label = super.getDropDownView(position, convertView, parent) as TextView
//        label.setTextColor(Color.BLACK)
//        label.text = values[position].CourierName
//
//        return label

        if (convertView != null) {
            ivCourierImage = convertView?.findViewById(R.id.ivCourierImage)
            tvName = convertView?.findViewById(R.id.tvName)
            tvMobile = convertView?.findViewById(R.id.tvMobile)
            tvHaveTaskNow = convertView?.findViewById(R.id.tvHaveTaskNow)
            progress = convertView?.findViewById(R.id.progress)

        }
        val courier = getItem(position)
        if (courier != null) {
            if(!courier.PickupName.isNullOrEmpty()&&!courier.DropoffName.isNullOrEmpty())
            {
                val descriptionData = arrayOf(
                    courier.PickupName,
                    courier.DropoffName)
                progress!!.setStateDescriptionData(descriptionData)
                progress!!.visibility=View.VISIBLE

            }
            if(courier.VehicleTypeID==2)
                ivCourierImage!!.setImageResource(R.drawable.bike)


            tvName!!.text = courier.CourierName
            tvMobile!!.text = courier.CourierMobile
            if (courier.HasTasksNow) {
//                convertView.setBackgroundColor(
//                    Color.parseColor("#008CDB")
//                )
                tvHaveTaskNow!!.text = "Has Task"
            } else tvHaveTaskNow!!.text =
                "No Task"
        }
        return convertView!!

    }
}
