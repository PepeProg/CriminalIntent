package com.example.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*

class MainActivity : AppCompatActivity(), CrimeFragmentList.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (existingFragment == null) {
            val fragment = CrimeFragmentList.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onCrimeSelected(uuid: UUID) {
        val fragment = CrimeFragment.newInstance(uuid)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)           //allow using button back
            .commit()

    }
}