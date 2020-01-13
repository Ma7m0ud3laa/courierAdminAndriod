package com.twoam.agent.ticket

import android.content.Context
import android.os.Bundle

import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twoam.agent.R
import com.twoam.agent.adapter.TicketAdapter
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.agent.model.DropOffLocation
import com.twoam.agent.model.PickUpLocation
import com.twoam.agent.model.Stop
import com.twoam.agent.model.Ticket
import com.twoam.agent.utilities.AppController
import com.twoam.cartello.Utilities.Base.BaseFragment


class TicketFragment : BaseFragment(), IBottomSheetCallback {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var ticket: Ticket = Ticket()
    private var rvTasks: RecyclerView? = null
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
        var view = inflater.inflate(R.layout.fragment_ticket, container, false)
        init(view)
        return view
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
        rvTasks = view.findViewById(R.id.rvTasks)
        getTickets()

    }

    private fun getTickets() {
        var stopList = ArrayList<Stop>()
        stopList.add(
            Stop(
                "1",
                "City Stars",
                "Blue Bag",
                PickUpLocation("1", "City Stars", 30.073239, 31.346265)
            )
        )
        stopList.add(
            Stop(
                "2",
                "City Mall",
                "Blue Bag",
                PickUpLocation("1", "City Mall", 30.073239, 31.346265)
            )
        )
        stopList.add(
            Stop(
                "3",
                "City Grand",
                "Blue Bag",
                PickUpLocation("1", "City Grand", 30.073239, 31.346265)
            )
        )
        stopList.add(
            Stop(
                "4",
                "City DD",
                "Blue Bag",
                PickUpLocation("1", "City DD", 30.073239, 31.346265)
            )
        )

        var ticketList = ArrayList<Ticket>()


        ticketList.add(
            Ticket(
                "Kadabra",
                "New Task",
                "Buy this bag from city stars.",
                "NEW",
                PickUpLocation("1", "City Stars", 1.02154, 1.02454),
                DropOffLocation("1", "Maddi", 1.02154, 1.02454),
                stopList
            )
        )
        ticketList.add(
            Ticket(
                "Kadabra",
                "New Task",
                "Ask about this item price.",
                "NEW",
                PickUpLocation("1", "Town down", 1.02154, 1.02454),
                DropOffLocation("1", "Maddi", 1.02154, 1.02454),
                stopList
            )
        )
        ticketList.add(
            Ticket(
                "Kadabra",
                "New Task",
                "Reserve flight ticket to usa.",
                "NEW",
                PickUpLocation("1", "eagle reservation agency.", 1.02154, 1.02454),
                DropOffLocation("1", "tahrir", 1.02154, 1.02454),
                stopList
            )
        )
        ticketList.add(
            Ticket(
                "Kadabra",
                "New Task",
                "Buy this bag from city stars.",
                "NEW",
                PickUpLocation("1", "City Stars", 1.02154, 1.02454),
                DropOffLocation("1", "Maddi", 1.02154, 1.02454),
                stopList
            )
        )
        ticketList.add(
            Ticket(
                "Kadabra",
                "New Task",
                "Ask about this item price.",
                "NEW",
                PickUpLocation("1", "Town down", 1.02154, 1.02454),
                DropOffLocation("1", "Maddi", 1.02154, 1.02454),
                stopList
            )
        )
        ticketList.add(
            Ticket(
                "Kadabra",
                "New Task",
                "Reserve flight ticket to usa.",
                "NEW",
                PickUpLocation("1", "eagle reservation agency.", 1.02154, 1.02454),
                DropOffLocation("1", "tahrir", 1.02154, 1.02454),
                stopList
            )
        )
        ticketList.add(
            Ticket(
                "Kadabra",
                "New Task",
                "Buy this bag from city stars.",
                "NEW",
                PickUpLocation("1", "City Stars", 1.02154, 1.02454),
                DropOffLocation("1", "Maddi", 1.02154, 1.02454),
                stopList
            )
        )
        ticketList.add(
            Ticket(
                "Kadabra",
                "New Task",
                "Ask about this item price.",
                "NEW",
                PickUpLocation("1", "Town down", 1.02154, 1.02454),
                DropOffLocation("1", "Maddi", 1.02154, 1.02454),
                stopList
            )
        )
        ticketList.add(
            Ticket(
                "Kadabra",
                "New Task",
                "Reserve flight ticket to usa.",
                "NEW",
                PickUpLocation("1", "eagle reservation agency.", 1.02154, 1.02454),
                DropOffLocation("1", "tahrir", 1.02154, 1.02454),
                stopList
            )
        )


        var adapter = TicketAdapter(context!!, ticketList,listener!!)
        rvTasks?.adapter = adapter
        rvTasks?.layoutManager =
            GridLayoutManager(AppController.getContext(), 1, GridLayoutManager.VERTICAL, false)
    }
}// Required empty public constructor
