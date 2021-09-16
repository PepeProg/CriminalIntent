package com.example.criminalintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel: ViewModel() {
    val crimeList = mutableListOf<Crime>()

    init {
        for (i in 1..100) {
            val crime = Crime()
            crime.apply {
                title = "Crime number $i"
                isSolved = i % 2 == 1
            }
            crimeList += crime
        }
    }
}