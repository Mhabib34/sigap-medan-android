package com.example.smart_city

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.smart_city.fragment.BerandaFragment
import com.example.smart_city.fragment.HadiahFragment
import com.example.smart_city.fragment.HeatmapFragment
import com.example.smart_city.fragment.MisiFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // Load fragment awal
        loadFragment(BerandaFragment())

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_beranda -> loadFragment(BerandaFragment())
                R.id.nav_misi -> loadFragment(MisiFragment())
                R.id.nav_hadiah -> loadFragment(HadiahFragment())
                R.id.navigation_heatmap -> loadFragment(HeatmapFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}