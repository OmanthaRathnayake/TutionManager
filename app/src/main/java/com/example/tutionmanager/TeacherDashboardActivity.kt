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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TeacherDashboardActivity: AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().reference

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var spinnerCourses: Spinner
    private var selectedCourseId: String? = null

    private val courseMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.teacher_bottom_nav)
        spinnerCourses = findViewById(R.id.spinnerCourses)

        loadAssignedCourses()

        bottomNav.setOnItemSelectedListener { item ->

            if (selectedCourseId == null) {
                Toast.makeText(this, "Please select a course first", Toast.LENGTH_SHORT).show()
                return@setOnItemSelectedListener false
            }


            when (item.itemId) {
                R.id.nav_attendance -> {
                    loadFragment(AttendanceFragment.newInstance(selectedCourseId!!))
                    true
                }
                R.id.nav_assignments -> {
                    loadFragment(AssignmentsFragment.newInstance(selectedCourseId!!))
                    true
                }
                R.id.nav_materials -> {
                    loadFragment(MaterialsFragment.newInstance(selectedCourseId!!))
                    true
                }
                R.id.nav_results -> {
                    loadFragment(ResultsFragment.newInstance(selectedCourseId!!))
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
    private fun loadAssignedCourses() {
        val teacherId = auth.currentUser!!.uid
        dbRef.child("courses").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                courseMap.clear()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val name = child.child("name").value.toString()
                    val assignedTeacher = child.child("teacherId").value.toString()
                    if (assignedTeacher == teacherId) {
                        courseMap[id] = name
                    }
                }

                val courseNames = courseMap.values.toList()
                val adapterSpinner = ArrayAdapter(this@TeacherDashboardActivity, android.R.layout.simple_spinner_item, courseNames)
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCourses.adapter = adapterSpinner

                spinnerCourses.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedCourseId = courseMap.keys.toList()[position]
                        Toast.makeText(this@TeacherDashboardActivity, "Selected: ${courseNames[position]}", Toast.LENGTH_SHORT).show()


                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.teacher_fragment_container, fragment)
            .commit()
    }



}