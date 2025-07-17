package com.example.tutionmanager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity: AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        bottomNav = findViewById(R.id.bottom_nav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_users -> {
                    loadFragment(UsersFragment())
                    true
                }
                R.id.nav_courses -> {
                    loadFragment(CoursesFragment())
                    true
                }
                R.id.nav_reports -> {
                    loadFragment(ReportsFragment())
                    true
                }
                R.id.nav_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Load default fragment
        bottomNav.selectedItemId = R.id.nav_users
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
    }
}