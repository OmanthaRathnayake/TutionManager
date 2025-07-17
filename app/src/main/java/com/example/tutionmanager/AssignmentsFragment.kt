package com.example.tutionmanager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.ValueEventListener


class AssignmentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddAssignment: FloatingActionButton
    private lateinit var adapter: AssignmentAdapter

    private val assignmentList = mutableListOf<Assignment>()

    private val dbRef = FirebaseDatabase.getInstance().getReference("assignments")
    private val storageRef = FirebaseStorage.getInstance().getReference("assignment_files")

    private val PICK_FILE_REQUEST = 101
    private var selectedFileUri: Uri? = null
    private var selectedAssignmentId: String? = null

    private lateinit var courseId: String

    companion object {
        fun newInstance(courseId: String): AssignmentsFragment {
            val fragment = AssignmentsFragment()
            val args = Bundle()
            args.putString("courseId", courseId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        courseId = arguments?.getString("courseId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assignments, container, false)

        recyclerView = view.findViewById(R.id.recyclerAssignments)
        fabAddAssignment = view.findViewById(R.id.fabAddAssignment)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AssignmentAdapter(assignmentList, ::showEditDialog, ::deleteAssignment){ assignment ->
            val fragment = SubmissionsFragment.newInstance(courseId, assignment.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.teacher_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = adapter

        fabAddAssignment.setOnClickListener {
            showAddDialog()
        }

        loadAssignments()

        return view
    }

    private fun loadAssignments() {
        dbRef.child(courseId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                assignmentList.clear()
                for (child in snapshot.children) {
                    val assignment = child.getValue(Assignment::class.java)
                    if (assignment != null) {
                        assignmentList.add(assignment)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_assignment, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val btnUploadFile = dialogView.findViewById<Button>(R.id.btnUploadFile)

        selectedFileUri = null

        btnUploadFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Assignment")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString()
                val description = etDescription.text.toString()
                val id = dbRef.child(courseId).push().key!!

                if (selectedFileUri != null) {
                    uploadFile(selectedFileUri!!, id, title, description)
                } else {
                    val assignment = Assignment(id, title, description, null)
                    dbRef.child(courseId).child(id).setValue(assignment).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Assignment added", Toast.LENGTH_SHORT).show()
                        loadAssignments()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(assignment: Assignment) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_assignment, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val btnUploadFile = dialogView.findViewById<Button>(R.id.btnUploadFile)

        etTitle.setText(assignment.title)
        etDescription.setText(assignment.description)
        selectedFileUri = null

        btnUploadFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Assignment")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedTitle = etTitle.text.toString()
                val updatedDesc = etDescription.text.toString()

                if (selectedFileUri != null) {
                    uploadFile(selectedFileUri!!, assignment.id, updatedTitle, updatedDesc)
                } else {
                    val updatedAssignment = assignment.copy(title = updatedTitle, description = updatedDesc)
                    dbRef.child(courseId).child(assignment.id).setValue(updatedAssignment)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show()
                            loadAssignments()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadFile(uri: Uri, id: String, title: String, description: String) {
        val fileRef = storageRef.child("$id-${System.currentTimeMillis()}")
        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { url ->
                val assignment = Assignment(id, title, description, url.toString())
                dbRef.child(courseId).child(id).setValue(assignment).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Uploaded with file", Toast.LENGTH_SHORT).show()
                    loadAssignments()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "File upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAssignment(assignment: Assignment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete")
            .setMessage("Delete ${assignment.title}?")
            .setPositiveButton("Yes") { _, _ ->
                dbRef.child(courseId).child(assignment.id).removeValue().addOnSuccessListener {
                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                    loadAssignments()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            Toast.makeText(requireContext(), "File Selected", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
