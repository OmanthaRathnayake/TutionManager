package com.example.tutionmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseAdapter (
    private val courses: List<Course>,
    private val teacherMap: Map<String, String>, // uid to name
    private val onEdit: (Course) -> Unit,
    private val onDelete: (Course) -> Unit
): RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tvCourseName)
        val teacher = view.findViewById<TextView>(R.id.tvTeacherName)
        val edit = view.findViewById<Button>(R.id.btnEdit)
        val delete = view.findViewById<Button>(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(v)
    }

    override fun getItemCount(): Int = courses.size

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.name.text = course.name
        holder.teacher.text = "Teacher: ${teacherMap[course.teacherId] ?: "N/A"}"
        holder.edit.setOnClickListener { onEdit(course) }
        holder.delete.setOnClickListener { onDelete(course) }
    }
}
