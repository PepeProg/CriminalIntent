package com.example.criminalintent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CrimeFragmentList() : Fragment() {
    private lateinit var recyclerView: RecyclerView
    val listViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        recyclerView = view.findViewById(R.id.crime_fragment_recycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        updateUI()
        return view
    }

    private inner class Holder(view: View): RecyclerView.ViewHolder(view) {  //Class, working with views
        private lateinit var crime: Crime
        val titleText: TextView = itemView.findViewById(R.id.crime_title)
        val dateText: TextView = itemView.findViewById(R.id.date_field)
        fun bind(crime: Crime) {
            this.crime = crime
            titleText.text = this.crime.title
            dateText.text = this.crime.date.toString()
        }
    }

    private inner class HolderAdapter(val crimeList: List<Crime>)
        :RecyclerView.Adapter<Holder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): Holder {
                val view = layoutInflater.inflate(R.layout.view_of_list, parent, false)
                return Holder(view)
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                val crime = crimeList[position]
                holder.bind(crime)
            }

            override fun getItemCount() = crimeList.size
        }

    private fun updateUI() {
        recyclerView.adapter = HolderAdapter(listViewModel.crimeList)

    }

    companion object {
        fun newInstance(): CrimeFragmentList{
            return CrimeFragmentList()
        }
    }
}