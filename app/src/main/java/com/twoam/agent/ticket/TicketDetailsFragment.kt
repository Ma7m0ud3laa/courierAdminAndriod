package com.twoam.agent.ticket

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.twoam.agent.R
import com.twoam.agent.adapter.StopAdapter
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.agent.model.Stop
import com.twoam.agent.utilities.AppConstants
import com.twoam.agent.utilities.AppController
import com.twoam.cartello.Utilities.Base.BaseFragment


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TicketDetailsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TicketDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TicketDetailsFragment : BaseFragment(), IBottomSheetCallback, View.OnClickListener {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private  var tvUserNameData: TextView?=null
    private  var tvTaskDetails: TextView?=null
    private var tvPickUpData: TextView?=null
    private var tvDropOffData: TextView?=null
    private var rvStops: RecyclerView?=null
    lateinit var currentView: View


    private var listener: IBottomSheetCallback? = null

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
        // Inflate the layout for this fragment
        init()
        currentView = inflater.inflate(R.layout.fragment_ticket_details, container, false)

        if (AppConstants.currentSelectedTicket != null) {
            loadTicketDetails()
        }
        return currentView
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvPickUpData, R.id.tvDropOffData -> {
                listener!!.onBottomSheetSelectedItem(4)
            }
        }

    }


    private fun init() {

        tvUserNameData = currentView!!.findViewById(R.id.tvUserNameData)
        tvTaskDetails = currentView!!.findViewById(R.id.tvTaskDetails)
        tvPickUpData = currentView!!.findViewById(R.id.tvPickUpData)
        tvDropOffData = currentView!!.findViewById(R.id.tvDropOffData)
        rvStops = currentView!!.findViewById(R.id.rvStops)

        tvPickUpData!!.setOnClickListener(this)
        tvDropOffData!!.setOnClickListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBottomSheetSelectedItem(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun loadTicketDetails() {
        var task = AppConstants.currentSelectedTicket
        tvUserNameData!!.text = task.userName
        tvTaskDetails!!.text = task.description
        tvPickUpData!!.text =task.pickUpLocation.name
//            task.pickUpLocation.lat.toString() + "," + task.pickUpLocation.lon.toString()
        tvDropOffData!!.text =task.dropOffLocation.name
//            task.dropOffLocation.lat.toString() + "," + task.dropOffLocation.lon.toString()
        loadTaskStops(task.stopList!!)
    }

    private fun loadTaskStops(stopList: ArrayList<Stop>) {

        var adapter = StopAdapter(parentFragment!!.context!!, stopList)
        rvStops!!.adapter = adapter
        rvStops!!.layoutManager =
            LinearLayoutManager(AppController.getContext(), LinearLayoutManager.HORIZONTAL, false)

    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TicketDetailsFragment.
         */
//        // TODO: Rename and change types and number of parameters
//        fun newInstance(param1: String, param2: String): TicketDetailsFragment {
//            val fragment = TicketDetailsFragment()
//            val args = Bundle()
//            args.putString(ARG_PARAM1, param1)
//            args.putString(ARG_PARAM2, param2)
//            fragment.arguments = args
//            return fragment
//        }
    }
}// Required empty public constructor
