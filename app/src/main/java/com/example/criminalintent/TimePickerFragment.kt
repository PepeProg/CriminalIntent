package com.example.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*



class TimePickerFragment: DialogFragment() {
    private lateinit var currentDate: Date

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        currentDate = arguments?.getSerializable(DATE_ARG) as Date
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val timeListener = TimePickerDialog.OnTimeSetListener {_, hours, minutes->
            val calendar = Calendar.getInstance().apply {time = currentDate}
            calendar.set(Calendar.HOUR_OF_DAY, hours)
            calendar.set(Calendar.MINUTE, minutes)
            val result = Bundle().apply {
                putSerializable(DATE_ARG, calendar.time)
            }
            parentFragmentManager.setFragmentResult(DIALOG_DATE_ID, result)
        }
        return TimePickerDialog(
            requireContext(),
            timeListener,
            hours,
            minutes,
            true
        )

    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(DATE_ARG, date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}