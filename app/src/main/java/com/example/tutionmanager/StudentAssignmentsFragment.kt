package com.example.tutionmanager

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.ValueEventListener


class StudentAssignmentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentAssignmentAdapter
    private val assignmentList = mutableListOf<Assignment>()

    private val dbRef = FirebaseDatabase.getInstance().getReference("assignments")
    private val submissionsRef = FirebaseDatabase.getInstance().getReference("submissions")
    private val storageRef = FirebaseStorage.getInstance().getReference("submissions")
    private lateinit var auth: FirebaseAuth

    private val PICK_FILE_REQUEST = 102
    private var selectedAssignmentId: String? = null
    private var selectedFileUri: Uri? = null

    private lateinit var courseId: String

    companion object {
        fun newInstance(courseId: String): StudentAssignmentsFragment {
            val fragment = StudentAssignmentsFragment()
            val args = Bundle()
            args.putString("courseId", courseId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        courseId = arguments?.getString("courseId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_assignments, container, false)
        recyclerView = view.findViewById(R.id.recyclerStudentAssignments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = StudentAssignmentAdapter(
            assignmentList,
            onDownload = { fileUrl -> downloadAssignment(fileUrl) },
            onSubmit = { assignment -> selectFileForSubmission(assignment.id) }
        )

        recyclerView.adapter = adapter

        loadAssignments()
        return view
    }

    private fun loadAssignments() {
        dbRef.child(courseId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                assignmentList.clear()
                for (child in snapshot.children) {
                    val assignment = child.getValue(Assignment::class.java)
                    if (assignment != null) assignmentList.add(assignment)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load assignments", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun downloadAssignment(fileUrl: String?) {
        if (fileUrl != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "No file attached", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectFileForSubmission(assignmentId: String) {
        selectedAssignmentId = assignmentId
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, PICK_FILE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            uploadSubmission()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadSubmission() {
        if (selectedFileUri == null || selectedAssignmentId == null) {
            Toast.makeText(requireContext(), "Please select a file", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser!!.uid
        val fileRef = storageRef.child("$courseId/${selectedAssignmentId}/${userId}.pdf")

        fileRef.putFile(selectedFileUri!!)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { url ->
                    val submissionData = mapOf(
                        "fileUrl" to url.toString(),
                        "timestamp" to System.currentTimeMillis()
                    )
                    submissionsRef.child(courseId)
                        .child(selectedAssignmentId!!)
                        .child(userId)
                        .setValue(submissionData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Submitted Successfully", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


