package com.kadabra.agent.adapter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import android.widget.FrameLayout
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import com.kadabra.Networking.NetworkManager
import com.kadabra.agent.R
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.model.Notification
import com.kadabra.agent.utilities.AppConstants




/**
 * Created by Mokhtar on 1/8/2020.
 */

class NotificationAdapter(
    private val context: Context,
    private val NotificationsList: ArrayList<Notification>, private val _taskListener: IBottomSheetCallback
    ) :
    RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var notification: Notification = Notification()
    private var listener: IBottomSheetCallback? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.MyViewHolder {
        listener = _taskListener

        if (viewType == 1)//default layout
        {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_layout, parent, false)
            return MyViewHolder(view)
        } else {
            val view1 =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_layout_readed, parent, false)
            return MyViewHolder(view1)
        }
    }


    override fun onBindViewHolder(holder: NotificationAdapter.MyViewHolder, position: Int) {
        notification = NotificationsList[position]


        holder.tvTitle.text = notification.notificationTitle
        holder.tvSubject.text = notification.notificationContent
        holder.tvDate.text = notification.notificationDate

    }

    override fun getItemCount(): Int {
        return NotificationsList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


    override fun getItemViewType(position: Int): Int {
//        return position
var notification=NotificationsList[position]
        return if (notification.isReaded) {
            2 // READED

        } else
            1//UNREADED

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        var tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        var tvDate: TextView =
            itemView.findViewById(R.id.tvDate)


        init {


            itemView.setOnClickListener {
                //                if (NetworkManager().isNetworkAvailable(context)) {
                val pos = adapterPosition
                notification = NotificationsList[pos]
               AppConstants.CurrentSelecedNotification=notification
//                listener!!.onBottomSheetSelectedItem(15)//go to notification view


            }


        }



    }
}
