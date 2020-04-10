package com.kadabra.agent.utilities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.DatePicker
import android.widget.EditText

import java.util.Calendar
import java.util.TimeZone

/**
 * Created by Mokhtar on 6/24/2019.
 */

class CustomEditTextDatePicker(private val _context: Context, editTextViewID: Int) : View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private var _editText: EditText
    private var _day: Int = 0
    private var _month: Int = 0
    private var _birthYear: Int = 0

    init {
        val act = _context as Activity
        this._editText = act.findViewById<View>(editTextViewID) as EditText
        this._editText.setOnClickListener(this)
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        _birthYear = year
        _month = monthOfYear
        _day = dayOfMonth
        updateDisplay()
    }

    override fun onClick(v: View) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        if(_birthYear!=0) calendar.set(_birthYear,_month,_day);
        val dialog = DatePickerDialog(_context, this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
        dialog.show()

    }

    // updates the date in the birth date EditText
    private fun updateDisplay() {

        _editText.setText(StringBuilder()
                // Month is 0 based so add 1
                .append(_birthYear).append("-").append(_month + 1).append("-").append(_day).append(" "))
    }
}
