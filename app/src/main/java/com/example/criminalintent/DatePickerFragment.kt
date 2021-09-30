package com.example.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*


class DatePickerFragment: DialogFragment() {   //class showing dialog menu to choose new date
    private lateinit var currentDate: Date

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        currentDate = arguments?.getSerializable(DATE_ARG) as Date
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dateListener = DatePickerDialog.OnDateSetListener {_, year, month, day->   //this function is called after choosing date
            val calendar = Calendar.getInstance().apply {time = currentDate}
            calendar.apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }
            val result = Bundle().apply {
                putSerializable(DATE_ARG, calendar.time)
            }
            parentFragmentManager.setFragmentResult(DIALOG_DATE_ID, result)      //this method returns result to all subscribed listeners
        }
        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(DATE_ARG, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }

    }
}