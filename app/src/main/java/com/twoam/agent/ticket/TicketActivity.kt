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
import com.twoam.agent.R
import com.twoam.agent.callback.ITaskCallback
import com.twoam.agent.model.Stop
import com.twoam.agent.model.Task
import com.twoam.agent.task.NewTaskFragment
import com.twoam.agent.task.TaskDetailsFragment
import com.twoam.agent.task.TasksFragment



class TicketActivity : AppCompatActivity(), IBottomSheetCallback,ITaskCallback {
    //  region Members
    private var ticketFragment: TicketFragment = TicketFragment()
    private var newTaskFragment: NewTaskFragment = NewTaskFragment()
    private var ticketDetailsFragment: TicketDetailsFragment = TicketDetailsFragment()
    private var tasksFragment: TasksFragment = TasksFragment()
    private var taskDetailsFragment: TaskDetailsFragment = TaskDetailsFragment()
    private var courierFragment: CourierFragment = CourierFragment()
    private var notificationFragment: NotificationFragment = NotificationFragment()
    private val fm = supportFragmentManager
    private var active = BaseFragment()
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

    override fun onBackPressed() {
        if (active == ticketFragment)
            finish()
        else if (active == newTaskFragment&&!newTaskFragment.editMode) {
            fm.beginTransaction()
                .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                .commit()
            active = ticketFragment
            toggleFabMode()
        }
        else if (active == newTaskFragment&&newTaskFragment.editMode) {
            fm.beginTransaction()
                .replace(R.id.layout_container, ticketDetailsFragment, "ticketDetailsFragment")
                .commit()
            active = ticketDetailsFragment
        }

        else if (active == ticketDetailsFragment) {
            fm.beginTransaction()
                .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                .commit()
            active = ticketFragment
        } else if (active == tasksFragment) {
            fm.beginTransaction()
                .replace(R.id.layout_container, ticketDetailsFragment, "ticketDetailsFragment")
                .commit()
            active = ticketDetailsFragment
        } else if (active == taskDetailsFragment) {
            fm.beginTransaction()
                .replace(R.id.layout_container, tasksFragment, "tasksFragment")
                .commit()
            active = tasksFragment
        } else if (active == courierFragment) {
            fm.beginTransaction()
                .replace(R.id.layout_container, ticketFragment, "ticketFragment")
            active = ticketFragment
        } else if (active == notificationFragment) {
            fm.beginTransaction()
                .replace(R.id.layout_container, ticketFragment, "ticketFragment")
            active = ticketFragment
        } else if (fm.backStackEntryCount > 0) {

        } else {
            super.onBackPressed()
        }

//        if (fm.backStackEntryCount > 0) {
//            Log.i("MainActivity", "popping backstack")
//            fm.popBackStack()
//        } else {
//            Log.i("MainActivity", "nothing on backstack, calling super")
//            super.onBackPressed()
//        }
//        toggleFabMode()
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {

    }

    override fun onBottomSheetSelectedItem(index: Int) {

        when (index) {
            0 ->//open  add new task
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                    .commit()
                active = ticketFragment
                toggleFabMode()
            }
            1 -> //back from new ticket view
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                    .commit()
                active = ticketFragment
                toggleFabMode()

            }
            2 ->// back from Notification view
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                    .commit()
                active = ticketFragment
                toggleFabMode()

            }
            3 -> //open ticket details from adapter
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, ticketDetailsFragment, "ticketDetailsFragment")
                    .commit()
                active = ticketDetailsFragment

            }

            4 -> //open map to show the current pick or drop off locations
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, courierFragment, "courierFragment")
                    .commit()
                active = courierFragment

            }
            5 -> // open tasks from ticket
            {
                fm.beginTransaction()
                    .replace(R.id.layout_container, tasksFragment, "tasksFragment")
                    .commit()
                active = tasksFragment

            }
            6 -> // open tasks view in edit mode
            {
                newTaskFragment.editMode=true
                fm.beginTransaction()
                    .replace(R.id.layout_container, newTaskFragment, "newTaskFragment")
                    .commit()
                active = newTaskFragment

            }

            7 -> // add  new task to the current ticket
            {
                newTaskFragment.editMode=false
                fm.beginTransaction()
                    .replace(R.id.layout_container, newTaskFragment, "newTaskFragment")
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
                    .commit()
                active = ticketFragment
                toggleFabMode()

            }

            11 ->// navigation to ticket fragments
            {
              if(active!=ticketFragment)
              {
                  fm.beginTransaction()
                      .replace(R.id.layout_container, ticketFragment, "ticketFragment")
                      .commit()
                  active = ticketFragment
              }
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
    private fun init() {

        setUpBottomAppBar()


        fm.beginTransaction()
            .replace(R.id.layout_container, ticketFragment, "ticketFragment")
            .commit()
        active = ticketFragment

        fab.setOnClickListener {

            when (active) {
                ticketFragment -> {
                    fm.beginTransaction()
                        .replace(
                            R.id.layout_container,
                            newTaskFragment,
                            "newTaskFragment"
                        )
                        .commit()
                    active = newTaskFragment
                    toggleFabMode()
                }
                newTaskFragment -> {
                    fm.beginTransaction()
                        .replace(
                            R.id.layout_container,
                            ticketFragment,
                            "ticketFragment"
                        )
                        .commit()
                    active = ticketFragment
                    toggleFabMode()
                }
                ticketDetailsFragment -> {
                    fm.beginTransaction()
                        .replace(
                            R.id.layout_container,
                            ticketFragment,
                            "ticketFragment"
                        )
                        .commit()
                    active = ticketFragment
                }
                courierFragment -> {
                    fm.beginTransaction()
                        .replace(
                            R.id.layout_container,
                            ticketFragment,
                            "ticketFragment"
                        )
                        .commit()
                    active = ticketFragment
                }
                notificationFragment -> {
                    fm.beginTransaction()
                        .replace(
                            R.id.layout_container,
                            ticketFragment,
                            "ticketFragment"
                        )
                        .commit()
                    active = ticketFragment
                }
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
                    toggleFabMode()
                    fm.beginTransaction()
                        .replace(
                            R.id.layout_container,
                            notificationFragment,
                            "notificationFragment"
                        )
                        .commit()
                    active = notificationFragment


                }
                R.id.action_find_couriers -> {
                    toggleFabMode()
                    fm.beginTransaction()
                        .replace(R.id.layout_container, courierFragment, "courierFragment")
                        .commit()
                    active = courierFragment


                }
                R.id.action_filter -> {

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
        ft.replace(R.id.layout_container, fragment, tag)
        ft.commitAllowingStateLoss()
    }

    private fun toggleFabMode() {
        //check the fab alignment mode and toggle accordingly
        if (bottomAppBar?.fabAlignmentMode == BottomAppBar.FAB_ALIGNMENT_MODE_END) {
            bottomAppBar?.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
            fab.setImageResource(R.drawable.ic_add_black_24dp)

        } else {
            bottomAppBar?.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
            fab.setImageResource(R.drawable.ic_back)
        }
    }


//endregion

}
