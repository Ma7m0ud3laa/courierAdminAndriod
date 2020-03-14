package com.kadabra.agent.ticket

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.kadabra.agent.callback.BottomSheetNavigationFragment
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.courier.CourierFragment
import com.kadabra.agent.notification.NotificationFragment
import kotlinx.android.synthetic.main.activity_ticket.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kadabra.Networking.NetworkManager
import com.kadabra.Utilities.Base.BaseFragment
import com.kadabra.agent.R
import com.kadabra.agent.callback.ITaskCallback
import com.kadabra.agent.login.LoginActivity
import com.kadabra.agent.model.Stop
import com.kadabra.agent.model.Task
import com.kadabra.agent.task.NewTaskFragment
import com.kadabra.agent.task.TasksFragment
import com.kadabra.agent.utilities.Alert
import com.kadabra.agent.utilities.AppConstants


class TicketActivity : AppCompatActivity(), IBottomSheetCallback, ITaskCallback {
    //  region Members
    private var ticketFragment: TicketFragment = TicketFragment()
    private var newTaskFragment: NewTaskFragment = NewTaskFragment()
    private var newTicketFragment: NewTicketFragment = NewTicketFragment()
    private var tasksFragment: TasksFragment = TasksFragment()
    private var courierFragment: CourierFragment = CourierFragment()
    private var notificationFragment: NotificationFragment = NotificationFragment()
    private val fm = supportFragmentManager
    private var active = BaseFragment()
    val bottomSheetDialogFragment = BottomSheetNavigationFragment.newInstance()
    //endregion

    //region Events
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket)
        init()

    }

    //Inflate menu to bottom bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {

        when (active) {

            ticketFragment -> finish()  //HOME

            notificationFragment -> { //NOTIFICATION
                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                    .commit()
                active = ticketFragment
            }
            courierFragment -> { //MAP

                if (courierFragment.searchMode) //TASK VIEW
                {
                    fm.beginTransaction()
                        .replace(R.id.layout_container, newTaskFragment, "newTaskFragment")
                    active = newTaskFragment
                } else //MAP VIEW [ALL COURIERS]
                {
                    fm.beginTransaction()
                        .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                        .commit()
                    active = ticketFragment
                }

            }

            newTicketFragment -> {
                newTicketFragment.editMode = false
                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                    .commit()
                active = ticketFragment
                fab.visibility=View.VISIBLE

//                fm.beginTransaction()
//                    .detach(newTicketFragment)
//                    .attach(newTicketFragment)
//                    .commit()
//                active = ticketFragment

            }


            //
            newTaskFragment -> {
                fm.beginTransaction()
                    .replace(R.id.layout_container, newTicketFragment, "newTicketFragment")
                    .commit()
                active = newTicketFragment
            }
            //
            //        else if (active == newTicketFragment) {
            //            fm.beginTransaction()
            //                .replace(R.paymentId.layout_container, ticketFragment, "ticketFragment")
            //                .commit()
            //            active = ticketFragment
            //
            //        } else if (active == tasksFragment) {
            //            fm.beginTransaction()
            //                .replace(R.paymentId.layout_container, newTicketFragment, "newTicketFragment")
            //                .commit()
            //            active = newTicketFragment
            //        } else if (active == taskDetailsFragment) {
            //            fm.beginTransaction()
            //                .replace(R.paymentId.layout_container, ticketFragment, "ticketFragment")
            //                .commit()
            //            active = ticketFragment
            //        } else if (active == courierFragment && !courierFragment.searchMode) {
            //            fm.beginTransaction()
            //                .replace(active.paymentId, ticketFragment, "ticketFragment")
            //            active = ticketFragment
            //        } else if (active == courierFragment && courierFragment.searchMode) {
            //            fm.beginTransaction()
            //                .replace(R.paymentId.layout_container, newTaskFragment, "newTaskFragment")
            //            active = newTaskFragment
            //        } else if (active == notificationFragment) {
            //            fm.beginTransaction()
            //                .replace(R.paymentId.layout_container, ticketFragment, "ticketFragment")
            //            active = ticketFragment
            //        } else if (fm.backStackEntryCount > 0) {
            //
            //        } else {
            //            super.onBackPressed()
            //        }


            //        if (fm.backStackEntryCount > 0) {
            ////            if (active == courierFragment)
            ////               onBottomSheetSelectedItem(13)
            //            active = ticketFragment
            //            fm.popBackStack()
            //        } else {
            //            super.onBackPressed()
            //        }
        }
//
//        else if (active == newTicketFragment) {
//            fm.beginTransaction()
//                .replace(R.paymentId.layout_container, ticketFragment, "ticketFragment")
//                .commit()
//            active = ticketFragment
//
//        } else if (active == tasksFragment) {
//            fm.beginTransaction()
//                .replace(R.paymentId.layout_container, newTicketFragment, "newTicketFragment")
//                .commit()
//            active = newTicketFragment
//        } else if (active == taskDetailsFragment) {
//            fm.beginTransaction()
//                .replace(R.paymentId.layout_container, ticketFragment, "ticketFragment")
//                .commit()
//            active = ticketFragment
//        } else if (active == courierFragment && !courierFragment.searchMode) {
//            fm.beginTransaction()
//                .replace(active.paymentId, ticketFragment, "ticketFragment")
//            active = ticketFragment
//        } else if (active == courierFragment && courierFragment.searchMode) {
//            fm.beginTransaction()
//                .replace(R.paymentId.layout_container, newTaskFragment, "newTaskFragment")
//            active = newTaskFragment
//        } else if (active == notificationFragment) {
//            fm.beginTransaction()
//                .replace(R.paymentId.layout_container, ticketFragment, "ticketFragment")
//            active = ticketFragment
//        } else if (fm.backStackEntryCount > 0) {
//
//        } else {
//            super.onBackPressed()
//        }


//        if (fm.backStackEntryCount > 0) {
////            if (active == courierFragment)
////               onBottomSheetSelectedItem(13)
//            active = ticketFragment
//            fm.popBackStack()
//        } else {
//            super.onBackPressed()
//        }

    }

    override fun onBottomSheetClosed(isClosed: Boolean) {

    }

    @SuppressLint("RestrictedApi")
    override fun onBottomSheetSelectedItem(index: Int) {

        when (index) {
            0 ->//
            {

                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                    .addToBackStack(null)
                    .commit()
                newTicketFragment.editMode = false

                active = ticketFragment

                fab.visibility=View.VISIBLE
            }
            1 -> //back from new ticket view
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                    .addToBackStack(null)
                    .commit()
                fab.visibility=View.VISIBLE
                active = ticketFragment
                toggleFabMode()

            }
            2 ->// back from Notification view
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                    .addToBackStack(null)
                    .commit()
                fab.visibility=View.VISIBLE
                active = ticketFragment
//                toggleFabMode()

            }
            3 -> //open ticket details from adapter
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, newTicketFragment, "newTicketFragment")
                    .addToBackStack(null)
                    .commit()
                newTicketFragment.editMode = AppConstants.CurrentLoginAdmin.IsSuperAdmin
                active = newTicketFragment
                fab.visibility=View.INVISIBLE

            }

            4 -> //open map to show the current pick or drop off locations
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, courierFragment, "courierFragment")
                    .addToBackStack(null)
                    .commit()
                active = courierFragment

            }
            5 -> // open tasks from ticket
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, tasksFragment, "tasksFragment")
                    .addToBackStack(null)
                    .commit()
                active = tasksFragment

            }
            6 -> // open tasks view in edit mode
            {
                newTaskFragment = NewTaskFragment()
                fm.beginTransaction()
                    .replace(R.id.layout_container, newTaskFragment, "newTaskFragment")
                    .addToBackStack(null)
                    .commit()
                newTaskFragment.editMode = true
                active = newTaskFragment

            }

            7 -> // add  new task to the current ticket
            {
                newTaskFragment = NewTaskFragment()
                newTaskFragment.editMode = false
                fm.beginTransaction()
                    .replace(R.id.layout_container, newTaskFragment, "newTaskFragment")
                    .addToBackStack(null)
                    .commit()
                active = newTaskFragment

            }
            8 -> // open map view for choose location for stop
            {
                courierFragment.searchMode = true

                if (fm.findFragmentByTag("newTaskFragment") != null) {

                    //hide new task view
                    fm.beginTransaction().hide(active).commit()
                    //show courier view
                    fm.beginTransaction()
                        .add(R.id.layout_container, courierFragment, "courierFragment")
                        .addToBackStack(null)
                        .commit()
                    active = courierFragment
                }


            }
            9 -> // set the stop data to the new task view
            {
                if (active == courierFragment) {
                    //show new task view
                    fm.beginTransaction().show(newTaskFragment)
                        .commit()
                    //remove courier view
                    fm.beginTransaction().remove(courierFragment).commit()

                    active = newTaskFragment
                }

            }

            10 -> // close the new task view after add new task successfully
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                    .addToBackStack(null)
                    .commit()
                fab.visibility=View.VISIBLE
                active = ticketFragment
                toggleFabMode()

            }

            11 ->// navigation to ticket fragments
            {
                if (active != ticketFragment) {
                    fm.beginTransaction()
                        .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                        .addToBackStack(null)
                        .commit()
                    fab.visibility=View.VISIBLE
                    active = ticketFragment
                    fab.visibility=View.VISIBLE
                }
            }

            12 -> //delete task
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, newTicketFragment, "newTicketFragment")
                    .addToBackStack(null)
                    .commit()
                active = newTicketFragment


                fm.popBackStack(
                    fm.getBackStackEntryAt(fm.backStackEntryCount - 1).id,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }

            13 -> //show new task again after hide mode
            {
//                fm.beginTransaction()
//                    .replace(R.paymentId.layout_container, newTicketFragment, "newTicketFragment")
//                    .addToBackStack(null)
//                    .commit()
//                active = newTicketFragment

                fm.popBackStack(
                    fm.getBackStackEntryAt(fm.backStackEntryCount - 1).id,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }

            14 -> // LOG OUT
            {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }


        }
    }

    override fun onTaskDelete(task: Task?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStopDelete(stop: Stop?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //endregion

    //region Helper Functions
    @SuppressLint("RestrictedApi")
    private fun init() {

        setUpBottomAppBar()

        fm.beginTransaction()
            .replace(R.id.layout_container, ticketFragment, "ticketFragment")
            .commit()
        active = ticketFragment
        fab.visibility=View.VISIBLE

        fab.setOnClickListener {

            if (AppConstants.CurrentLoginAdmin.IsSuperAdmin && active != newTicketFragment) {
                newTicketFragment = NewTicketFragment()
                fm.beginTransaction()
                    .replace(
                        R.id.layout_container,
                        newTicketFragment,
                        "newTicketFragment"
                    ).addToBackStack(null)
                    .commit()

                active = newTicketFragment

                fab.visibility=View.INVISIBLE
            }
//            else if (active == newTicketFragment && newTicketFragment.editMode) {
//                newTaskFragment.editMode = false
//                newTaskFragment.taskAddMode = true
//
//                fm.beginTransaction()
//                    .replace(
//                        R.id.layout_container,
//                        newTaskFragment,
//                        "newTaskFragment"
//                    ).addToBackStack(null)
//                    .commit()
//                active = newTaskFragment
//            }
//


            when (active) {

//                ticketFragment -> {
//
//                    newTaskFragment.editMode = false
//                    newTaskFragment.taskAddMode = true
//
//                    fm.beginTransaction()
//                        .replace(
//                            R.paymentId.layout_container,
//                            newTaskFragment,
//                            "newTaskFragment"
//                        ).addToBackStack(null)
//                        .commit()
//                    active = newTaskFragment
//                    toggleFabMode()
//
//                }
//                newTaskFragment -> {
//                    fm.beginTransaction()
//                        .replace(
//                            R.paymentId.layout_container,
//                            ticketFragment,
//                            "ticketFragment"
//                        ).addToBackStack(null)
//                        .commit()
//                    active = ticketFragment
//                    toggleFabMode()
//                }
//                newTicketFragment -> {
//                    fm.beginTransaction()
//                        .replace(
//                            R.paymentId.layout_container,
//                            ticketFragment,
//                            "ticketFragment"
//                        )
//                        .commit()
//                    active = ticketFragment
//                }
//                courierFragment -> {
//                    fm.beginTransaction()
//                        .replace(
//                            R.paymentId.layout_container,
//                            ticketFragment,
//                            "ticketFragment"
//                        )
//                        .commit()
//                    active = ticketFragment
//                }
//                notificationFragment -> {
//                    fm.beginTransaction()
//                        .replace(
//                            R.paymentId.layout_container,
//                            ticketFragment,
//                            "ticketFragment"
//                        )
//                        .commit()
//                    active = ticketFragment
//                }
            }

//            toggleFabMode()


        }


    }

    private fun setUpBottomAppBar() {
        //set bottom bar to Action bar as it is similar like Toolbar
        setSupportActionBar(bottomAppBar)

        //click event over Bottom bar menu item
        bottomAppBar?.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notification -> {
//                    toggleFabMode()
                    fm.beginTransaction()
                        .replace(
                            R.id.layout_container,
                            notificationFragment,
                            "notificationFragment"
                        ).addToBackStack(null)
                        .commit()
                    active = notificationFragment


                }
                R.id.action_find_couriers -> {
//                    toggleFabMode()
                    if (NetworkManager().isNetworkAvailable(this)) {
                        courierFragment.searchMode = false
                        fm.beginTransaction()
                            .replace(R.id.layout_container, courierFragment, "courierFragment")
                            .addToBackStack(null)
                            .commit()
                        active = courierFragment
                    } else
                        Alert.showMessage(this, getString(R.string.no_internet))


                }
                R.id.action_filter -> {

                    //only  filter on ticket view
                    if (active == ticketFragment) {
                        //todo show context menu with filteration typess
                        Toast.makeText(
                            this@TicketActivity,
                            "Filter Data is clicked.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }


            }

            false

        }
        )

        //click event over navigation menu like back arrow or hamburger icon
        bottomAppBar?.setNavigationOnClickListener(View.OnClickListener {
            //open bottom sheet
//            val bottomSheetDialogFragment = BottomSheetNavigationFragment.newInstance()
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
        ft.replace(R.id.layout_container, fragment, tag)
        ft.commitAllowingStateLoss()
    }

    private fun toggleFabMode() {
        //check the fab alignment mode and toggle accordingly
//        if (bottomAppBar?.fabAlignmentMode == BottomAppBar.FAB_ALIGNMENT_MODE_END) {
//            bottomAppBar?.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
//            fab.setImageResource(R.drawable.ic_add_black_24dp)
//
//        } else {
//            bottomAppBar?.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
//            fab.setImageResource(R.drawable.ic_back)
//        }
    }


//endregion

}
