package com.example.tutionmanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAttendanceAdapter (
    private val attendanceList: List<Pair<String, String>>
) : RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.tvDate)
        val status: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_attendance, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (date, status) = attendanceList[position]
        holder.date.text = date
        holder.status.text = status.capitalize()
        holder.status.setTextColor(if (status == "present") Color.parseColor("#4CAF50") else Color.RED)
    }

    override fun getItemCount(): Int = attendanceList.size
}