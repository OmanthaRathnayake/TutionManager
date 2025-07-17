package com.example.tutionmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResultsFragment : Fragment() {

    private lateinit var spinnerStudents: Spinner
    private lateinit var etMarks: EditText
    private lateinit var btnUpload: Button

    private val dbRef = FirebaseDatabase.getInstance().reference
    private val studentMap = mutableMapOf<String, String>() // uid -> name

    private lateinit var courseId: String

    companion object {
        fun newInstance(courseId: String): ResultsFragment {
            val fragment = ResultsFragment()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)

        spinnerStudents = view.findViewById(R.id.spinnerStudents)
        etMarks = view.findViewById(R.id.etMarks)
        btnUpload = view.findViewById(R.id.btnUploadResult)

        loadStudents()

        btnUpload.setOnClickListener { uploadResult() }

        return view
    }

    private fun loadStudents() {
        dbRef.child("users").orderByChild("role").equalTo("student")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    studentMap.clear()
                    for (child in snapshot.children) {
                        val uid = child.key ?: continue
                        val name = child.child("fullName").value.toString()
                        studentMap[uid] = name
                    }

                    val studentNames = studentMap.values.toList()
                    val adapterStudent = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, studentNames)
                    spinnerStudents.adapter = adapterStudent
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun uploadResult() {
        val marksStr = etMarks.text.toString()
        if (marksStr.isEmpty()) {
            Toast.makeText(requireContext(), "Enter marks", Toast.LENGTH_SHORT).show()
            return
        }
        val marks = marksStr.toInt()
        val studentId = studentMap.keys.toList()[spinnerStudents.selectedItemPosition]

        dbRef.child("results").child(studentId).child(courseId).setValue(marks)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Result uploaded!", Toast.LENGTH_SHORT).show()
                etMarks.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Upload failed!", Toast.LENGTH_SHORT).show()
            }
    }
}

