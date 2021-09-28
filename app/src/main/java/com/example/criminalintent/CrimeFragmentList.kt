package com.example.criminalintent

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
import java.text.DateFormat

private const val TAG = "CrimeListFragment"

class CrimeFragmentList : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private var adapter = HolderAdapter(emptyList())  //initializing an adapter with empty list, later will filled in data from database
    private val listViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
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
                    Log.i(TAG, "Got crimes")
                    updateUI(crimes)
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
                Toast.makeText(context, "${crime.title}", Toast.LENGTH_SHORT).show()
            }
            if (itemView.id == R.id.danger_view_of_list) {
                policeButton = itemView.findViewById(R.id.police_button)
                policeButton.setOnClickListener {
                    Toast.makeText(context, "The police is coming", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fun bind(crime: Crime) {
            this.crime = crime
            titleText.text = this.crime.title
            val formatDate = DateFormat.getInstance()
            dateText.text = formatDate.format(this.crime.date)
            isSolvedImage.visibility =
                if (this.crime.isSolved)
                    View.VISIBLE
                else
                    View.GONE
        }
    }

    private inner class HolderAdapter(val crimeList: List<Crime>)
        :RecyclerView.Adapter<Holder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): Holder {
                val viewId = if (viewType == 0)
                    R.layout.view_of_list
                else
                    R.layout.danger_view_of_list
                val view = layoutInflater.inflate(viewId, parent, false)
                return Holder(view)
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                val crime = crimeList[position]
                holder.bind(crime)
            }

            override fun getItemCount() = crimeList.size

            override fun getItemViewType(position: Int): Int {
//                if (crimeList[position].isDangerous)
//                    return 1
                return 0
            }
        }

    private fun updateUI(crimeList: List<Crime>) {
        adapter = HolderAdapter(crimeList)
        recyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): CrimeFragmentList{
            return CrimeFragmentList()
        }
    }
}