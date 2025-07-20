package com.example.tutionmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentResultAdapter (
    private val results: List<Pair<String, Int>>
) : RecyclerView.Adapter<StudentResultAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseName: TextView = view.findViewById(R.id.tvCourseName)
        val marks: TextView = view.findViewById(R.id.tvMarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_result, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (course, mark) = results[position]
        holder.courseName.text = course
        holder.marks.text = mark.toString()
    }

    override fun getItemCount(): Int = results.size
}