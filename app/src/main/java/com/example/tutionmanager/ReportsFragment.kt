package com.example.tutionmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ReportsFragment : Fragment() {

    private lateinit var btnAttendance: Button
    private lateinit var btnResults: Button
    private lateinit var spinnerCourses: Spinner
    private lateinit var recyclerView: RecyclerView

    private val courseMap = mutableMapOf<String, String>() // id -> name
    private val studentMap = mutableMapOf<String, String>() // uid -> fullName

    private val reportList = mutableListOf<Pair<String, String>>() // name, summary
    private lateinit var adapter: ReportAdapter

    private val dbRef = FirebaseDatabase.getInstance().reference

    private var currentMode = "attendance"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reports, container, false)

        btnAttendance = view.findViewById(R.id.btnAttendance)
        btnResults = view.findViewById(R.id.btnResults)
        spinnerCourses = view.findViewById(R.id.spinnerCourses)
        recyclerView = view.findViewById(R.id.recyclerReport)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReportAdapter(reportList)
        recyclerView.adapter = adapter

        btnAttendance.setOnClickListener {
            currentMode = "attendance"
            loadReports()
        }

        btnResults.setOnClickListener {
            currentMode = "results"
            loadReports()
        }

        loadCoursesAndStudents()

        return view
    }

    private fun loadCoursesAndStudents() {
        dbRef.child("courses").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                courseMap.clear()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val name = child.child("name").value.toString()
                    courseMap[id] = name
                }

                val courseNames = courseMap.values.toList()
                val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseNames)
                spinnerCourses.adapter = adapterSpinner

                dbRef.child("users").orderByChild("role").equalTo("student")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            studentMap.clear()
                            for (child in snapshot.children) {
                                val uid = child.key ?: continue
                                val name = child.child("fullName").value.toString()
                                studentMap[uid] = name
                            }
                            loadReports()
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadReports() {
        reportList.clear()

        val selectedCourseIndex = spinnerCourses.selectedItemPosition
        if (selectedCourseIndex == -1) return

        val selectedCourseId = courseMap.keys.toList()[selectedCourseIndex]

        if (currentMode == "attendance") {
            dbRef.child("attendance").child(selectedCourseId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (studentSnapshot in snapshot.children) {
                            val uid = studentSnapshot.key ?: continue
                            val name = studentMap[uid] ?: "Unknown"
                            var total = 0
                            var present = 0

                            for (dateSnapshot in studentSnapshot.children) {
                                total++
                                if (dateSnapshot.value.toString().lowercase() == "present") {
                                    present++
                                }
                            }

                            val detail = "Present: $present / Total: $total"
                            reportList.add(name to detail)
                        }
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else if (currentMode == "results") {
            dbRef.child("results")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (studentSnapshot in snapshot.children) {
                            val uid = studentSnapshot.key ?: continue
                            val name = studentMap[uid] ?: "Unknown"
                            val courseResult = studentSnapshot.child(selectedCourseId).value
                            if (courseResult != null) {
                                val detail = "Marks: ${courseResult.toString()}"
                                reportList.add(name to detail)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }
}

