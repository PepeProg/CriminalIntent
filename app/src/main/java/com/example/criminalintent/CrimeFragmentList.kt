package com.example.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeFragmentList"

class CrimeFragmentList : Fragment() {
    /**
     * Required interface, which allows creating CrimeFragments
     */
    interface Callbacks {               //Activity must implement this method, to use creating new Fragments
        fun onCrimeSelected(uuid: UUID)
    }


    private var callbacks: Callbacks? = null
    private lateinit var recyclerView: RecyclerView
    private var adapter = HolderAdapter()  //initializing an adapter with empty list, later will filled in with data from database
    private val listViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        recyclerView = view.findViewById(R.id.crime_fragment_recycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listViewModel.crimeListLiveData.observe(  //creating an observer for getting data from database
            viewLifecycleOwner,                   //matching observing with fragment's view lifecycle (Fragment implements LifecycleOwner)
            Observer {crimes ->
                crimes?.let{
                    updateUI(it)
                }
            }
        )
    }

    private inner class Holder(view: View): RecyclerView.ViewHolder(view) {  //Class, working with views
        private lateinit var crime: Crime
        private val titleText: TextView = itemView.findViewById(R.id.crime_title)
        private val dateText: TextView = itemView.findViewById(R.id.date_field)
        private lateinit var policeButton: Button
        private val isSolvedImage: ImageView = itemView.findViewById(R.id.crime_is_solved)
        init {
            itemView.setOnClickListener {
                callbacks?.onCrimeSelected(crime.id)    //call activity-overriding function, which will do everything
            }
            if (itemView.id == R.id.danger_view_of_list) {
                policeButton = itemView.findViewById(R.id.police_button)
                policeButton.setOnClickListener {
                    Toast.makeText(context, "The police is coming", Toast.LENGTH_SHORT).show()
                }
            }
        }

        /*
        This method can work, but, using it, fragment matches to certain Activity, so in
        this case using interface Callbacks, which encapsulate this logic, will be better

        fun onClick(view: View) {
            val fragment = CrimeFragment.newInstance(crime.id)
            val fM = activity.supportFragmentManager
            fM.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

        }
        */


        fun bind(crime: Crime) {
            this.crime = crime
            titleText.text = this.crime.title
            val formatDate = SimpleDateFormat.getInstance()
            dateText.text = formatDate.format(this.crime.date)
            isSolvedImage.visibility =
                if (this.crime.isSolved)
                    View.VISIBLE
                else
                    View.GONE
        }
    }

    private inner class HolderAdapter()   //List adapter can update only changed data
        :ListAdapter<Crime,Holder>(DiffCallBack()){
            override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): Holder {
                val viewId = if (viewType == 0)
                    R.layout.view_of_list
                else
                    R.layout.danger_view_of_list
                val view = layoutInflater.inflate(viewId, parent, false)
                return Holder(view)
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                val crime = getItem(position)
                holder.bind(crime)
            }

            override fun getItemViewType(position: Int): Int {
//                if (crimeList[position].isDangerous)
//                    return 1
                return 0
            }

        }

    private class DiffCallBack(): DiffUtil.ItemCallback<Crime>() {       //helps ListAdapter to compare data
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return (oldItem.title == newItem.title) &&
                    (oldItem.isSolved == newItem.isSolved) &&
                    (oldItem.date == newItem.date)
        }
    }

    private fun updateUI(crimeList: List<Crime>) {
        adapter.submitList(crimeList)
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    companion object {
        fun newInstance(): CrimeFragmentList{
            return CrimeFragmentList()
        }
    }
}