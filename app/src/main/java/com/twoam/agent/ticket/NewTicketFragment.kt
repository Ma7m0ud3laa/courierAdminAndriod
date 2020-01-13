package com.twoam.agent.ticket


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.twoam.agent.R
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.cartello.Utilities.Base.BaseFragment

class NewTicketFragment : BaseFragment(), IBottomSheetCallback {

    private var btnBack: ImageView? = null
    private var listener: IBottomSheetCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(
            R.layout.fragment_new_ticket, container, false
        )
            btnBack = view.findViewById(R.id.btnBack)
        btnBack!!.setOnClickListener {
            listener?.onBottomSheetSelectedItem(1)
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement IBottomSheetCallback.onBottomSheetSelectedItem")
        }
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBottomSheetSelectedItem(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
