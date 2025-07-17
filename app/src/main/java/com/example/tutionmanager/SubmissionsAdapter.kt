package com.example.tutionmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubmissionsAdapter (
    private val submissions: List<Pair<String, String>>, // name, fileUrl
    private val onDownload: (String) -> Unit
) : RecyclerView.Adapter<SubmissionsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvStudentName)
        val download: Button = view.findViewById(R.id.btnDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_submission, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (studentName, fileUrl) = submissions[position]
        holder.name.text = studentName
        holder.download.setOnClickListener { onDownload(fileUrl) }
    }

    override fun getItemCount() = submissions.size
}