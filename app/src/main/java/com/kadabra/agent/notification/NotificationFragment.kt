package com.kadabra.agent.notification

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.kadabra.Networking.INetworkCallBack
import com.kadabra.Networking.NetworkManager
import com.kadabra.agent.api.ApiResponse
import com.kadabra.agent.api.ApiServices
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.R
import com.kadabra.agent.utilities.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kadabra.Utilities.Base.BaseFragment
import com.kadabra.agent.model.*
import kotlin.collections.ArrayList
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.kadabra.agent.adapter.NotificationAdapter



class NotificationFragment : BaseFragment(), IBottomSheetCallback {
    private var listener: IBottomSheetCallback? = null
    private var btnBack: ImageView? = null
    private var rvNotification: RecyclerView? = null
    private val TAG = "NotificationFragment"
    private lateinit var refresh: SwipeRefreshLayout
    private var notificationsList = NotificationData()
    private var tvTotalNotifications: TextView? = null
    private var tvEmptyData: TextView? = null
    private var tvTotalUnread: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_notification, container, false)
        btnBack = view.findViewById(R.id.ivBack)
        rvNotification = view.findViewById(R.id.rvNotifications)
        refresh = view!!.findViewById(com.kadabra.agent.R.id.refresh)
        tvTotalNotifications = view!!.findViewById(com.kadabra.agent.R.id.tvTotalNotifications)
        tvEmptyData = view!!.findViewById(com.kadabra.agent.R.id.tvEmptyData)
        tvTotalUnread = view!!.findViewById(com.kadabra.agent.R.id.tvTotalUnread)

        btnBack!!.setOnClickListener {
            listener?.onBottomSheetSelectedItem(2)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAllNotifications()

        refresh.setOnRefreshListener { loadAllNotifications() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {

    }

    override fun onBottomSheetSelectedItem(index: Int) {
        listener!!.onBottomSheetSelectedItem(2)
    }

    private fun loadAllNotifications() {
        Log.d(TAG, "loadNotifications: Enter method")

        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {

            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint =
                request.getALlNotifications(AppConstants.CurrentLoginAdmin.AdminId)
            NetworkManager().request(endPoint,
                object : INetworkCallBack<ApiResponse<NotificationData>> {
                    override fun onFailed(error: String) {
                        Log.d(TAG, "onFailed: " + error)
                        refresh.isRefreshing = false
                        Alert.hideProgress()
                        Alert.showMessage(
                            getString(R.string.error_login_server_error)
                        )
                    }

                    override fun onSuccess(response: ApiResponse<NotificationData>) {
                        Log.d(TAG, "onSuccess: Enter method")
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            Log.d(
                                TAG,
                                "onSuccess: AppConstants.STATUS_SUCCESS: " + AppConstants.STATUS_SUCCESS
                            )

                            notificationsList = response.ResponseObj!!

                            Log.d(
                                TAG,
                                "onSuccess" + notificationsList.adminNotificationModels!!.size.toString()
                            )

                            var notificationsListData = notificationsList.adminNotificationModels
                            if (notificationsListData!!.size > 0) {
                                Log.d(TAG, "onSuccess: notificationsList.size > 0: ")

                                tvTotalNotifications?.text =  notificationsList.NoOfUnreadedNotifications.toString()
                                tvTotalUnread?.text = notificationsList.NoOfUnreadedNotifications.toString()
//                                UserSessionManager.getInstance(context!!)
//                                    .setTotalNotification(notificationsList.NoOfUnreadedNotifications)
                                prepareNotifications(notificationsListData)
                                refresh.isRefreshing = false
                                tvEmptyData?.visibility = View.INVISIBLE
                                Alert.hideProgress()
                            } else {//no notifications
                                Log.d(TAG, "no Notifications: ")
                                refresh.isRefreshing = false
                                tvEmptyData?.visibility = View.VISIBLE
                                notificationsListData.clear()
                                prepareNotifications(notificationsListData)
                            }

                        } else {
                            Log.d(TAG, "onSuccess: Enter method")
                            refresh.isRefreshing = false
                            Alert.hideProgress()
                            tvEmptyData?.visibility = View.VISIBLE
                        }

                    }
                })

        } else {
            refresh.isRefreshing = false
            Alert.hideProgress()

            Alert.showMessage(
                getString(R.string.no_internet)
            )
        }


    }

    override fun onResume() {
        super.onResume()
        loadAllNotifications()
    }
    private fun prepareNotifications(Notifications: ArrayList<Notification>) {
        var adapter = NotificationAdapter(context!!, Notifications, this)
        rvNotification?.adapter = adapter
        rvNotification?.layoutManager =
            GridLayoutManager(
                context!!,
                1,
                GridLayoutManager.VERTICAL,
                false
            )
    }
}
