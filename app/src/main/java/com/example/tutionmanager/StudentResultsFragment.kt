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


class StudentResultsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentResultAdapter
    private val resultList = mutableListOf<Pair<String, Int>>()

    private val dbRef = FirebaseDatabase.getInstance().getReference("results")
    private val coursesRef = FirebaseDatabase.getInstance().getReference("courses")
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_results, container, false)
        recyclerView = view.findViewById(R.id.recyclerStudentResults)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = StudentResultAdapter(resultList)
        recyclerView.adapter = adapter

        loadResults()

        return view
    }

    private fun loadResults() {
        val userId = auth.currentUser!!.uid
        dbRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                resultList.clear()
                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                    return
                }

                val courseIds = mutableListOf<String>()
                val marksMap = mutableMapOf<String, Int>()

                for (child in snapshot.children) {
                    val courseId = child.key ?: continue
                    val marks = child.value.toString().toInt()
                    courseIds.add(courseId)
                    marksMap[courseId] = marks
                }

                // Fetch course names for these IDs
                coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(coursesSnap: DataSnapshot) {
                        for (id in courseIds) {
                            val courseName = coursesSnap.child(id).child("name").value?.toString() ?: "Unknown"
                            resultList.add(Pair(courseName, marksMap[id] ?: 0))
                        }
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    }

