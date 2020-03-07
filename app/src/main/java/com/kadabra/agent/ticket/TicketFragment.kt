package com.kadabra.agent.ticket

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kadabra.Networking.INetworkCallBack
import com.kadabra.Networking.NetworkManager
import com.kadabra.agent.R
import com.kadabra.agent.adapter.TicketAdapter
import com.kadabra.agent.api.ApiResponse
import com.kadabra.agent.api.ApiServices
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.model.Courier
import com.kadabra.agent.model.Ticket
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AppConstants
import com.kadabra.agent.utilities.AppController
import com.kadabra.cartello.Utilities.Base.BaseFragment
import kotlinx.android.synthetic.main.fragment_new_ticket.*
import kotlinx.android.synthetic.main.fragment_new_ticket.ivBack
import kotlinx.android.synthetic.main.fragment_ticket_details.*


class TicketFragment : BaseFragment(), IBottomSheetCallback {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var ticket: Ticket = Ticket()
    private var ticketList = ArrayList<Ticket>()
    private var rvTickets: RecyclerView? = null
    private var listener: IBottomSheetCallback? = null
    private var sRefresh: SwipeRefreshLayout? = null
    private var ivNoInternet: ImageView? = null
    private var tvEmptyData: TextView? = null


    var editMode = false
//    private var avi: AVLoadingIndicatorView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_ticket, container, false)


//        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        } else {
            throw ClassCastException("$context must implement IBottomSheetCallback.onBottomSheetSelectedItem")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): TicketFragment {
            val fragment = TicketFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBottomSheetSelectedItem(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun init(view: View) {
//        avi = view.findViewById(R.StopID.avi)
        rvTickets = view.findViewById(R.id.rvTickets)
        sRefresh = view.findViewById(R.id.sRefresh)
        ivNoInternet = view.findViewById(R.id.ivNoInternet)
        tvEmptyData = view.findViewById(R.id.tvEmptyData)



        sRefresh?.setOnRefreshListener {
            loadTickets()

        }

        if (AppConstants.GetALLTicket.count() > 0 && AppConstants.ALL_COURIERS.count() > 0) {
            prepareTicketData(AppConstants.GetALLTicket)
        } else {
            loadTickets()

        }

    }


    private fun loadTickets() {
        Alert.showProgress(context!!)
        if (NetworkManager().isNetworkAvailable(context!!)) {
            ivNoInternet!!.visibility = View.INVISIBLE
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getAllTickets()
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Ticket>>> {
                    override fun onFailed(error: String) {
                        sRefresh!!.isRefreshing = false
                        Alert.hideProgress()
                        Alert.showMessage(context!!, getString(R.string.error_login_server_error))
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Ticket>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            sRefresh!!.isRefreshing = false
                            tvEmptyData!!.visibility = View.INVISIBLE
                            ticketList = response.ResponseObj!!
                            AppConstants.GetALLTicket = ticketList
                            if (ticketList.size > 0) {
                                prepareTicketData(ticketList)
                                Alert.hideProgress()
                                getAllCouriers()
                            } else {//no taskModel
                                tvEmptyData!!.visibility = View.VISIBLE
                                Alert.hideProgress()
                            }

                        } else {
                            sRefresh!!.isRefreshing = false
                            Alert.hideProgress()
                            Alert.showMessage(
                                context!!,
                                getString(R.string.error_network)
                            )
                        }

                    }
                })

        } else {
            ivNoInternet!!.visibility = View.VISIBLE
            sRefresh!!.isRefreshing = false
            Alert.hideProgress()
            Alert.showMessage(context!!, getString(R.string.no_internet))
        }


    }

    private fun prepareTicketData(ticketList: ArrayList<Ticket>) {
        var adapter = TicketAdapter(context!!, ticketList, listener!!)
        rvTickets!!.adapter = adapter
        rvTickets?.layoutManager =
            GridLayoutManager(
                AppController.getContext(),
                1,
                GridLayoutManager.VERTICAL,
                false
            )
    }

    private fun getAllCouriers() {
        if (NetworkManager().isNetworkAvailable(context!!)) {
            var request = NetworkManager().create(ApiServices::class.java)
            var endPoint = request.getAllCouriers()
            NetworkManager().request(
                endPoint,
                object : INetworkCallBack<ApiResponse<ArrayList<Courier>>> {
                    override fun onFailed(error: String) {
                    }

                    override fun onSuccess(response: ApiResponse<ArrayList<Courier>>) {
                        if (response.Status == AppConstants.STATUS_SUCCESS) {
                            AppConstants.ALL_COURIERS = response.ResponseObj!!

                        }

                    }
                })

        } else {
        }
    }



}// Required empty public constructor
