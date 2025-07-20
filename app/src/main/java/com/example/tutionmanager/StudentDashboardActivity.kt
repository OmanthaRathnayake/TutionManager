package com.example.tutionmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StudentDashboardActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var spinnerCourses: Spinner
    private var selectedCourseId: String? = null

    private val courseMap = mutableMapOf<String, String>() // courseId -> courseName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        bottomNav = findViewById(R.id.student_bottom_nav)
        spinnerCourses = findViewById(R.id.spinnerStudentCourses)

        // Setup FAB
        val btnProfile = findViewById<FloatingActionButton>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.student_fragment_container, StudentProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        loadStudentCourses()

        bottomNav.setOnItemSelectedListener { item ->
            if ((item.itemId == R.id.nav_materials || item.itemId == R.id.nav_assignments) &&
                selectedCourseId == null
            ) {
                Toast.makeText(this, "Please select a course first", Toast.LENGTH_SHORT).show()
                return@setOnItemSelectedListener false
            }

            when (item.itemId) {
                R.id.nav_materials -> {
                    loadFragment(StudentMaterialsFragment.newInstance(selectedCourseId!!))
                    true
                }
                R.id.nav_assignments -> {
                    loadFragment(StudentAssignmentsFragment.newInstance(selectedCourseId!!))
                    true
                }
                R.id.nav_results -> {
                    loadFragment(StudentResultsFragment()) // No course filter needed
                    true
                }
                R.id.nav_attendance -> {
                    loadFragment(StudentAttendanceFragment()) // No course filter needed
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
    }

    private fun loadStudentCourses() {
        val coursesRef = FirebaseDatabase.getInstance().getReference("courses")

        coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                courseMap.clear()
                if (!snapshot.exists()) {
                    Toast.makeText(this@StudentDashboardActivity, "No courses available!", Toast.LENGTH_SHORT).show()
                    return
                }

                for (child in snapshot.children) {
                    val courseId = child.key ?: continue
                    val courseName = child.child("name").value?.toString() ?: "Unnamed Course"
                    courseMap[courseId] = courseName
                }

                setupCourseSpinner()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StudentDashboardActivity, "Failed to load courses", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCourseSpinner() {
        val courseNames = courseMap.values.toList()
        val adapterSpinner = ArrayAdapter(
            this@StudentDashboardActivity,
            android.R.layout.simple_spinner_item,
            courseNames
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourses.adapter = adapterSpinner

        spinnerCourses.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCourseId = courseMap.keys.toList()[position]
                refreshCurrentFragment()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun refreshCurrentFragment() {
        if (selectedCourseId == null) return
        val currentItem = bottomNav.selectedItemId
        when (currentItem) {
            R.id.nav_materials -> loadFragment(StudentMaterialsFragment.newInstance(selectedCourseId!!))
            R.id.nav_assignments -> loadFragment(StudentAssignmentsFragment.newInstance(selectedCourseId!!))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.student_fragment_container, fragment)
            .commit()
    }
}