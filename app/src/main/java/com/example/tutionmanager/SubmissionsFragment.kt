package com.example.tutionmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SubmissionsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubmissionsAdapter
    private val submissions = mutableListOf<Pair<String, String>>()

    private var courseId = ""
    private var assignmentId = ""
    private val dbRef = FirebaseDatabase.getInstance().reference
    private val studentMap = mutableMapOf<String, String>()

    companion object {
        fun newInstance(courseId: String, assignmentId: String): SubmissionsFragment {
            val fragment = SubmissionsFragment()
            val args = Bundle()
            args.putString("courseId", courseId)
            args.putString("assignmentId", assignmentId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_submissions, container, false)
        recyclerView = view.findViewById(R.id.recyclerSubmissions)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SubmissionsAdapter(submissions) { fileUrl ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        courseId = arguments?.getString("courseId") ?: ""
        assignmentId = arguments?.getString("assignmentId") ?: ""

        loadStudentsAndSubmissions()

        return view
    }

    private fun loadStudentsAndSubmissions() {
        dbRef.child("users").orderByChild("role").equalTo("student")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    studentMap.clear()
                    for (child in snapshot.children) {
                        val uid = child.key ?: continue
                        val name = child.child("fullName").value.toString()
                        studentMap[uid] = name
                    }
                    loadSubmissions()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadSubmissions() {
        dbRef.child("submissions").child(courseId).child(assignmentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    submissions.clear()
                    for (child in snapshot.children) {
                        val uid = child.key ?: continue
                        val name = studentMap[uid] ?: "Unknown"
                        val fileUrl = child.child("fileUrl").value.toString()
                        submissions.add(name to fileUrl)
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
    }

