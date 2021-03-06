package com.example.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.criminalintent.database.CrimeDatabase
import com.example.criminalintent.database.migration_1_2
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {

    private val database: CrimeDatabase = Room.databaseBuilder( //creating database with application context, pointing database class
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2)    //add any number of migrations
        .build()

    private val crimeDao = database.crimeDao()  //saving dao of the database

    private val executor = Executors.newSingleThreadExecutor()  //creating new background thread,
                            //because of having no possibility of using LiveData in Update-functions
    private val filesDir = context.applicationContext.filesDir

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()  //declaring functions from dao

    fun getCrime(uuid: UUID) : LiveData<Crime?> = crimeDao.getCrime(uuid)

    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    fun deleteCrime(crime: Crime) {
        executor.execute {
            crimeDao.deleteCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime) = File(filesDir, crime.photoFileName)

    companion object {
        private var INSTANCE: CrimeRepository? = null  //using singleton

        fun initialize(context: Context) {
            if (INSTANCE == null)
                INSTANCE = CrimeRepository(context)
        }

        fun get() : CrimeRepository {
            return INSTANCE ?:
                throw IllegalStateException("Repository must be initialized")
        }
    }
}