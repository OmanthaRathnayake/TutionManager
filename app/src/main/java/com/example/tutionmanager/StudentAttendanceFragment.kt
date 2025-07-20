package com.example.tutionmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class StudentAttendanceFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentAttendanceAdapter
    private val attendanceList = mutableListOf<Pair<String, String>>()

    private val dbRef = FirebaseDatabase.getInstance().getReference("attendance")
    private lateinit var auth: FirebaseAuth
    private lateinit var courseId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser!!.uid
        FirebaseDatabase.getInstance().getReference("users").child(userId)
            .child("courseId").get().addOnSuccessListener {
                courseId = it.value.toString()
                loadAttendance(userId)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_attendance, container, false)
        recyclerView = view.findViewById(R.id.recyclerStudentAttendance)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = StudentAttendanceAdapter(attendanceList)
        recyclerView.adapter = adapter

        return view
    }

    private fun loadAttendance(userId: String) {
        dbRef.child(courseId).child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                attendanceList.clear()
                for (child in snapshot.children) {
                    val date = child.key ?: continue
                    val status = child.value.toString()
                    attendanceList.add(Pair(date, status))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load attendance", Toast.LENGTH_SHORT).show()
            }
        })
    }
    }

