package com.twoam.agent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.twoam.agent.R
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.agent.callback.ITaskCallback
import com.twoam.agent.model.Stop
import com.twoam.agent.utilities.AppConstants

import java.util.ArrayList


/**
 * Created by Mokhtar on 1/8/2020.
 */

class StopAdapter(private val context: Context, private val stopList: ArrayList<Stop>,
                  private val _deleteTaskListener: ITaskCallback
) :
    RecyclerView.Adapter<StopAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var stop: Stop = Stop()
    private var listenerTask: ITaskCallback? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopAdapter.MyViewHolder {

        val view = inflater.inflate(R.layout.stop_layout, parent, false)
        listenerTask=_deleteTaskListener
        return MyViewHolder(view)


    }

    override fun onBindViewHolder(holder: StopAdapter.MyViewHolder, position: Int) {
        stop = stopList[position]

        holder.tvStopName.text = stop.StopName
        holder.tvStopType.text = stop.StopType


    }

    override fun getItemCount(): Int {
        return stopList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvStopName: TextView = itemView.findViewById(R.id.tvStopName)
        var tvStopType: TextView = itemView.findViewById(R.id.tvStopType)
        var tvDelete: TextView = itemView.findViewById(R.id.tvDelete)


        init {


            itemView.setOnClickListener {
                val pos = adapterPosition
                stop = stopList[pos]
                AppConstants.CurrentSelectedStop = stop
                //switch to map fragment
                //  context.startActivity(Intent(context, LocationDetailsActivity::class.java))

            }

            itemView.setOnLongClickListener {
                val pos = adapterPosition
                stop = stopList[pos]
                stop.status=2 //delete
                listenerTask!!.onStopDelete(stop)
//                var anim = AnimationUtils.loadAnimation(context, R.anim.shake)
//                itemView.startAnimation(anim)
                false
            }


            tvDelete.setOnClickListener {
                val pos = adapterPosition
                stop = stopList[pos]
                stop.status=2//delete
                listenerTask!!.onStopDelete(stop)
//                var anim = AnimationUtils.loadAnimation(context, R.anim.shake)
//                itemView.startAnimation(anim)
            }

        }
    }
}
