package com.example.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.criminalintent.database.CrimeDatabase
import java.lang.IllegalStateException
import java.util.*

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {

    private val database: CrimeDatabase = Room.databaseBuilder( //creating database with application context, pointing database class
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val crimeDao = database.crimeDao()  //saving dao of the database

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()  //declaring functions from dao

    fun getCrime(uuid: UUID) : LiveData<Crime?> = crimeDao.getCrime(uuid)

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