package com.example.tutionmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAssignmentAdapter (
    private val assignments: List<Assignment>,
    private val onDownload: (String?) -> Unit,
    private val onSubmit: (Assignment) -> Unit
) : RecyclerView.Adapter<StudentAssignmentAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvAssignmentTitle)
        val desc: TextView = view.findViewById(R.id.tvAssignmentDesc)
        val btnDownload: Button = view.findViewById(R.id.btnDownloadAssignment)
        val btnSubmit: Button = view.findViewById(R.id.btnSubmitAssignment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_assignment, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val assignment = assignments[position]
        holder.title.text = assignment.title
        holder.desc.text = assignment.description
        holder.btnDownload.setOnClickListener { onDownload(assignment.fileUrl) }
        holder.btnSubmit.setOnClickListener { onSubmit(assignment) }
    }

    override fun getItemCount(): Int = assignments.size
}