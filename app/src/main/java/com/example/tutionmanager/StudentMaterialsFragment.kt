package com.example.tutionmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class StudentMaterialsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentMaterialAdapter
    private val materialList = mutableListOf<Material>()
    private val dbRef = FirebaseDatabase.getInstance().getReference("materials")

    private lateinit var courseId: String

    companion object {
        fun newInstance(courseId: String): StudentMaterialsFragment {
            val fragment = StudentMaterialsFragment()
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
        val view = inflater.inflate(R.layout.fragment_student_materials, container, false)
        recyclerView = view.findViewById(R.id.recyclerStudentMaterials)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = StudentMaterialAdapter(materialList) { fileUrl ->
            downloadMaterial(fileUrl)
        }
        recyclerView.adapter = adapter

        loadMaterials()

        return view
    }

    private fun loadMaterials() {
        dbRef.child(courseId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                materialList.clear()
                for (child in snapshot.children) {
                    val material = child.getValue(Material::class.java)
                    if (material != null) materialList.add(material)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load materials", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun downloadMaterial(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}

