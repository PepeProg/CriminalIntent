package com.example.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.criminalintent.Crime
import java.util.*

@Dao //This annotation shows that funcs from this interface will be used to access the Database
     //Database access object
interface CrimeDao {
    @Query("SELECT * FROM crime") //Query gets SQL-command, which show all columns to all
                                        //crime-rows.
    fun getCrimes(): LiveData<List<Crime>>  //LiveData starts background thread

    @Query("SELECT * FROM crime WHERE id=(:id)") //Works as previous but except sorting by id
    fun getCrime(id: UUID): LiveData<Crime?>

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)

    @Delete
    fun deleteCrime(crime: Crime)
}