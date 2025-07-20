package com.example.tutionmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentMaterialAdapter (
    private val materials: List<Material>,
    private val onDownload: (String) -> Unit
) : RecyclerView.Adapter<StudentMaterialAdapter.ViewHolder>(){

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvMaterialTitle)
        val btnDownload: Button = view.findViewById(R.id.btnDownloadMaterial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_material, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val material = materials[position]
        holder.title.text = material.title
        holder.btnDownload.setOnClickListener { onDownload(material.fileUrl) }
    }

    override fun getItemCount(): Int = materials.size
}
