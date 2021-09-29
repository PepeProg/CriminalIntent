package com.example.criminalintent

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class CrimeFragmentViewModel() : ViewModel(){
    private val crimeRepository = CrimeRepository.get()
    private val crimeIDLiveData = MutableLiveData<UUID>()   //creating an empty id

    var crimeLiveData: LiveData<Crime?> = Transformations.switchMap(   //operator, working when first argument is changing
        crimeIDLiveData) {
        crimeRepository.getCrime(it)
    }

    fun loadCrime(uuid: UUID) {      //changes id, that triggers database query
        crimeIDLiveData.value = uuid
    }

    fun saveCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }
}