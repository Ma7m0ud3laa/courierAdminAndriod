package com.twoam.agent.ticket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.google.android.material.bottomappbar.BottomAppBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.twoam.agent.callback.BottomSheetNavigationFragment
import com.twoam.agent.callback.IBottomSheetCallback
import com.twoam.agent.courier.CourierFragment
import com.twoam.agent.notification.NotificationFragment
import com.twoam.cartello.Utilities.Base.BaseFragment
import kotlinx.android.synthetic.main.activity_ticket.*
import androidx.fragment.app.Fragment


class TicketActivity : AppCompatActivity(), IBottomSheetCallback {
    //  region Members
    private var ticketFragment: TicketFragment = TicketFragment()
    private var newTicketFragment: NewTicketFragment = NewTicketFragment()
    private var ticketDetailsFragment: TicketDetailsFragment = TicketDetailsFragment()
    private var courierFragment: CourierFragment = CourierFragment()
    private var notificationFragment: NotificationFragment = NotificationFragment()


    val fm = supportFragmentManager
    var active = BaseFragment()
    //endregion
    //region Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.twoam.agent.R.layout.activity_ticket)
        init()
    }


    //Inflate menu to bottom bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.twoam.agent.R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onBackPressed() {
        if (active == ticketFragment)
            finish()
        else if (active == newTicketFragment) {
            fm.beginTransaction()
                .replace(com.twoam.agent.R.id.layout_container, ticketFragment, "ticketFragment")
                .commit()
            active = ticketFragment
        } else if (active == ticketDetailsFragment) {
            fm.beginTransaction()
                .replace(com.twoam.agent.R.id.layout_container, ticketFragment, "ticketFragment")
            active = ticketFragment
        } else if (active == courierFragment) {
            fm.beginTransaction()
                .replace(com.twoam.agent.R.id.layout_container, ticketFragment, "ticketFragment")
            active = ticketFragment
        } else if (active == notificationFragment) {
            fm.beginTransaction()
                .replace(com.twoam.agent.R.id.layout_container, ticketFragment, "ticketFragment")
            active = ticketFragment
        } else if (fm.backStackEntryCount > 0) {

        } else {
            super.onBackPressed()
        }
        toggleFabMode()
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {

    }

    override fun onBottomSheetSelectedItem(index: Int) {

        when(index)
        {
            1 -> //back from new ticket view
            {
                fm.beginTransaction()
                    .replace(com.twoam.agent.R.id.layout_container, ticketFragment, "ticketFragment")
                    .commit()
                active = ticketFragment
                toggleFabMode()

            }
            2 ->// back from Notification view
            {
                fm.beginTransaction()
                    .replace(com.twoam.agent.R.id.layout_container, ticketFragment, "ticketFragment")
                    .commit()
                active = ticketFragment

            }
            3 -> //open ticket details from adapter
            {
//                fm.beginTransaction()
//                    .replace(com.twoam.agent.R.id.layout_container, ticketDetailsFragment, "ticketDetailsFragment")
//                    .commit()
//                active = ticketDetailsFragment

            }

            4 -> //open map to show the current pick or drop off locations
            {
                fm.beginTransaction()
                    .replace(com.twoam.agent.R.id.layout_container, courierFragment, "courierFragment")
                    .commit()
                active = courierFragment

            }


        }
    }

    //endregion
    //region Helper Functions
    private fun init() {

        setUpBottomAppBar()

//        if (NetworkManager().isNetworkAvailable(this)) {
        fm.beginTransaction()
            .replace(com.twoam.agent.R.id.layout_container, ticketFragment, "ticketFragment")
            .commit()
        active = ticketFragment

//        } else
//            Alert.showMessage(this, getString(R.string.no_internet))

        fab.setOnClickListener {

            when (active) {
                ticketFragment -> {
                    fm.beginTransaction()
                        .replace(
                            com.twoam.agent.R.id.layout_container,
                            newTicketFragment,
                            "newTicketFragment"
                        )
                        .commit()
                    active = newTicketFragment
                }
                newTicketFragment -> {
                    fm.beginTransaction()
                        .replace(
                            com.twoam.agent.R.id.layout_container,
                            ticketFragment,
                            "ticketFragment"
                        )
                        .commit()
                    active = ticketFragment
                }
                ticketDetailsFragment -> {
                    fm.beginTransaction()
                        .replace(
                            com.twoam.agent.R.id.layout_container,
                            ticketFragment,
                            "ticketFragment"
                        )
                        .commit()
                    active = ticketFragment
                }
                courierFragment -> {
                    fm.beginTransaction()
                        .replace(
                            com.twoam.agent.R.id.layout_container,
                            ticketFragment,
                            "ticketFragment"
                        )
                        .commit()
                    active = ticketFragment
                }
                notificationFragment -> {
                    fm.beginTransaction()
                        .replace(
                            com.twoam.agent.R.id.layout_container,
                            ticketFragment,
                            "ticketFragment"
                        )
                        .commit()
                    active = ticketFragment
                }
            }

            toggleFabMode()
        }

    }

    private fun setUpBottomAppBar() {
        //set bottom bar to Action bar as it is similar like Toolbar
        setSupportActionBar(bottomAppBar)

        //click event over Bottom bar menu item
        bottomAppBar?.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                com.twoam.agent.R.id.action_notification -> {
                    toggleFabMode()
                    fm.beginTransaction()
                        .replace(com.twoam.agent.R.id.layout_container, notificationFragment, "notificationFragment")
                        .commit()
                    active = notificationFragment


                }
                com.twoam.agent.R.id.action_find_couriers -> {
                    toggleFabMode()
                    fm.beginTransaction()
                        .replace(com.twoam.agent.R.id.layout_container, courierFragment, "courierFragment")
                        .commit()
                    active = courierFragment


                }
                com.twoam.agent.R.id.action_filter -> {

                    Toast.makeText(
                        this@TicketActivity,
                        "Filter Data is clicked.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //todo show context menu with filteration typess
                }


            }

            false

        }
        )

        //click event over navigation menu like back arrow or hamburger icon
        bottomAppBar?.setNavigationOnClickListener(View.OnClickListener {
            //open bottom sheet
            val bottomSheetDialogFragment = BottomSheetNavigationFragment.newInstance()
            bottomSheetDialogFragment.show(
                supportFragmentManager,
                "Bottom Sheet Dialog Fragment"
            )
        })
    }

    fun addFragment(fragment: Fragment, addToBackStack: Boolean, tag: String) {
        val manager = supportFragmentManager
        val ft = manager.beginTransaction()

        if (addToBackStack) {
            ft.addToBackStack(tag)
        }
        ft.replace(com.twoam.agent.R.id.layout_container, fragment, tag)
        ft.commitAllowingStateLoss()
    }

    fun toggleFabMode() {
        //check the fab alignment mode and toggle accordingly
        if (bottomAppBar?.fabAlignmentMode == BottomAppBar.FAB_ALIGNMENT_MODE_END) {
            bottomAppBar?.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
            fab.setImageResource(com.twoam.agent.R.drawable.ic_add_black_24dp)

        } else {
            bottomAppBar?.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
            fab.setImageResource(com.twoam.agent.R.drawable.ic_back)

        }
    }

//endregion


}
