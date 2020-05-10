package com.kadabra.agent.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import com.kadabra.agent.R
import com.kadabra.agent.model.Courier
import com.kofigyan.stateprogressbar.StateProgressBar


class CourierNewAdapter(
    context: Context,
    countryList: List<Courier>
) : ArrayAdapter<Courier?>(context, 0, countryList) {
    private var countryListFull = ArrayList<Courier>()
//    override fun getFilter(): Filter {
//        return countryFilter
//    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                R.layout.courier_layout, parent, false
            )
        }
        var imageViewFlag = convertView!!.findViewById<ImageView>(R.id.ivCourierImage);
        val tvName = convertView!!.findViewById<TextView>(R.id.tvName)
        val tvMobile = convertView.findViewById<TextView>(R.id.tvMobile)
        val tvHaveTaskNow = convertView.findViewById<TextView>(R.id.tvHaveTaskNow)
        val progress: StateProgressBar = convertView.findViewById(R.id.progress)
        val courier = getItem(position)
        if (courier != null) {
            if(!courier.PickupName.isNullOrEmpty()&&!courier.DropoffName.isNullOrEmpty())
            {
                val descriptionData = arrayOf(
                    courier.PickupName,
               courier.DropoffName)
                progress.setStateDescriptionData(descriptionData)
                progress.visibility=View.VISIBLE

            }
            if(courier.VehicleTypeID==2)
                imageViewFlag.setImageResource(R.drawable.bike)


            tvName.text = courier.CourierName
            tvMobile.text = courier.CourierMobile
            if (courier.HasTasksNow) {
//                convertView.setBackgroundColor(
//                    Color.parseColor("#008CDB")
//                )
                tvHaveTaskNow.text = "Has Task"
                tvHaveTaskNow.setTextColor(Color.parseColor("#E54728"))
            } else tvHaveTaskNow.text =
                "No Task"
        }
        return convertView!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getItem(position: Int): Courier? {
        return countryListFull[position]
    }

//    private val countryFilter: Filter = object : Filter() {
//        override fun performFiltering(constraint: CharSequence): FilterResults {
//            val results = FilterResults()
//            val suggestions: MutableList<Courier> =
//                ArrayList()
//            if (constraint == null || constraint.isEmpty()) {
//                suggestions.addAll(countryListFull)
//            } else {
//                countryListFull = ArrayList(countryList)
//                val filterPattern =
//                    constraint.toString().toLowerCase().trim { it <= ' ' }
//                for (item in countryListFull) {
//                    if (item.CourierName.toLowerCase().contains(filterPattern)) {
//                        suggestions.add(item)
//                    }
//                }
//            }
//            results.values = suggestions
//            results.count = suggestions.size
//            return results
//        }
//
//        override fun publishResults(
//            constraint: CharSequence?,
//            results: FilterResults?
//        ) {
//            clear()
//            if (results != null && results.count > 0) {
//                addAll(results.values as List<Courier>)
//                notifyDataSetChanged()
//            } else {
//                notifyDataSetInvalidated()
//            }
//
//
//        }
//
//        override fun convertResultToString(resultValue: Any): CharSequence {
//            return (resultValue as Courier).CourierName
//        }
//    }

    init {
        countryListFull = ArrayList(countryList)
    }
}