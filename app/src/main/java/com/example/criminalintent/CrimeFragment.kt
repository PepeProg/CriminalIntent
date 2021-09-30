package com.example.criminalintent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

private const val CRIME_ID = "crimeId"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
const val DIALOG_DATE_ID = "DialogDateId"
const val DATE_ARG = "DateArg"

class CrimeFragment : Fragment() {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var checkBox: CheckBox
    private val crimeFragmentViewModel: CrimeFragmentViewModel by lazy {
        ViewModelProvider(this).get(CrimeFragmentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val uuid: UUID = arguments?.get(CRIME_ID) as UUID
        crimeFragmentViewModel.loadCrime(uuid)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.text_edit) as EditText
        dateButton = view.findViewById(R.id.date_button) as Button
        timeButton = view.findViewById(R.id.time_button) as Button
        checkBox = view.findViewById(R.id.is_solved_checkbox) as CheckBox
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeFragmentViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { crime->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                crime.title = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }

        checkBox.setOnCheckedChangeListener { _, isSolved ->
            crime.isSolved = isSolved
        }

        titleField.addTextChangedListener(titleWatcher)

        dateButton.setOnClickListener {         //call dialog menu
            DatePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)  //first argument is fragment manager, that will call this dialog-fragment,
                                                                            //second one is id for fragment manager to identify it
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        parentFragmentManager.setFragmentResultListener(    //subscribing on getting results by id
            DIALOG_DATE_ID,
            viewLifecycleOwner
        ) {_, result->
            crime.date = result.getSerializable(DATE_ARG) as Date
            updateUI()
        }

    }

    override fun onStop() {
        super.onStop()
        crimeFragmentViewModel.saveCrime(crime)
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = SimpleDateFormat.getDateInstance().format(crime.date)
        timeButton.text = SimpleDateFormat.getTimeInstance().format(crime.date)
        checkBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()    //delete checkbox animation after getting crime from database
        }


    }

    companion object {
        fun newInstance(uuid: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(CRIME_ID, uuid)
            }
            val fragment = CrimeFragment()
            fragment.arguments = args      //adding arguments to the fragment, they will be saved during configuration's changing
            return fragment
        }
    }
}