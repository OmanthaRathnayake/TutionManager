package com.example.tutionmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AssignmentAdapter(
    private val assignments: List<Assignment>,
    private val onEdit: (Assignment) -> Unit,
    private val onDelete: (Assignment) -> Unit,
    private val onViewSubmissions: (Assignment) -> Unit
): RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {

    inner class AssignmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
        val btnViewSubmissions: Button = view.findViewById(R.id.btnViewSubmissions)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_assignment, parent, false)
        return AssignmentViewHolder(v)
    }
    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val assignment = assignments[position]
        holder.title.text = assignment.title
        holder.description.text = assignment.description
        holder.btnEdit.setOnClickListener { onEdit(assignment) }
        holder.btnDelete.setOnClickListener { onDelete(assignment) }
        holder.btnViewSubmissions.setOnClickListener { onViewSubmissions(assignment) }
    }
    override fun getItemCount(): Int = assignments.size
}