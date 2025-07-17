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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class CoursesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddCourse: Button

    private val courseList = mutableListOf<Course>()
    private val teacherMap = mutableMapOf<String, String>() // uid -> name

    private lateinit var adapter: CourseAdapter

    private val courseRef = FirebaseDatabase.getInstance().getReference("courses")
    private val userRef = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_courses, container, false)

        recyclerView = view.findViewById(R.id.recyclerCourses)
        btnAddCourse = view.findViewById(R.id.btnAddCourse)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CourseAdapter(courseList, teacherMap, ::showEditDialog, ::deleteCourse)
        recyclerView.adapter = adapter

        btnAddCourse.setOnClickListener {
            showAddDialog()
        }

        loadTeachersAndCourses()

        return view
    }

    private fun loadTeachersAndCourses() {
        userRef.orderByChild("role").equalTo("teacher")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    teacherMap.clear()
                    for (child in snapshot.children) {
                        val uid = child.key ?: continue
                        val name = child.child("fullName").value.toString()
                        teacherMap[uid] = name
                    }
                    loadCourses()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error loading teachers", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadCourses() {
        courseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                courseList.clear()
                for (child in snapshot.children) {
                    val course = child.getValue(Course::class.java)
                    if (course != null) {
                        courseList.add(course)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error loading courses", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_course, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCourseName)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerTeachers)

        val teacherList = teacherMap.entries.toList()
        val teacherNames = teacherList.map { it.value }

        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, teacherNames)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner

        AlertDialog.Builder(requireContext())
            .setTitle("Add Course")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val teacherId = teacherList.getOrNull(spinner.selectedItemPosition)?.key ?: return@setPositiveButton

                val id = courseRef.push().key!!
                val course = Course(id, name, teacherId)

                courseRef.child(id).setValue(course).addOnSuccessListener {
                    Toast.makeText(context, "Course added", Toast.LENGTH_SHORT).show()
                    loadCourses()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(course: Course) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_course, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCourseName)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerTeachers)

        etName.setText(course.name)

        val teacherList = teacherMap.entries.toList()
        val teacherNames = teacherList.map { it.value }

        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, teacherNames)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner

        // Set selected teacher
        val index = teacherList.indexOfFirst { it.key == course.teacherId }
        if (index >= 0) spinner.setSelection(index)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Course")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val name = etName.text.toString()
                val teacherId = teacherList.getOrNull(spinner.selectedItemPosition)?.key ?: return@setPositiveButton

                val updatedCourse = Course(course.id, name, teacherId)

                courseRef.child(course.id).setValue(updatedCourse).addOnSuccessListener {
                    Toast.makeText(context, "Course updated", Toast.LENGTH_SHORT).show()
                    loadCourses()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCourse(course: Course) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete ${course.name}?")
            .setPositiveButton("Yes") { _, _ ->
                courseRef.child(course.id).removeValue().addOnSuccessListener {
                    Toast.makeText(context, "Course deleted", Toast.LENGTH_SHORT).show()
                    loadCourses()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}

