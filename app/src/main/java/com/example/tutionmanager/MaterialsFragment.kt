package com.example.tutionmanager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.ValueEventListener


class MaterialsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddMaterial: FloatingActionButton
    private lateinit var adapter: MaterialAdapter


    private val materialList = mutableListOf<Material>()
    private val dbRef = FirebaseDatabase.getInstance().getReference("materials")
    private val storageRef = FirebaseStorage.getInstance().getReference("material_files")

    private val PICK_FILE_REQUEST = 101
    private var selectedFileUri: Uri? = null
    private var materialTitle: String = ""

    private lateinit var courseId: String


    companion object {
        fun newInstance(courseId: String): MaterialsFragment {
            val fragment = MaterialsFragment()
            val args = Bundle()
            args.putString("courseId", courseId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        courseId = arguments?.getString("courseId")?: ""
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_materials, container, false)

        recyclerView = view.findViewById(R.id.recyclerMaterials)
        fabAddMaterial = view.findViewById(R.id.fabAddMaterial)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MaterialAdapter(materialList, ::downloadMaterial, ::deleteMaterial)
        recyclerView.adapter = adapter

        fabAddMaterial.setOnClickListener {
            showAddDialog()
        }

        loadMaterials()

        return view
    }

    private fun loadMaterials() {
        dbRef.child(courseId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                materialList.clear()
                for (child in snapshot.children) {
                    val material = child.getValue(Material::class.java)
                    if (material != null) {
                        materialList.add(material)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load materials", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_material, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etMaterialTitle)
        val btnSelectFile = dialogView.findViewById<Button>(R.id.btnSelectFile)

        btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Upload Material")
            .setView(dialogView)
            .setPositiveButton("Upload") { _, _ ->
                materialTitle = etTitle.text.toString()
                if (materialTitle.isEmpty() || selectedFileUri == null) {
                    Toast.makeText(requireContext(), "Title or file missing", Toast.LENGTH_SHORT).show()
                } else {
                    uploadMaterial(selectedFileUri!!, materialTitle)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadMaterial(uri: Uri, title: String) {
        val id = dbRef.child(courseId).push().key!!
        val fileRef = storageRef.child("$id-${System.currentTimeMillis()}")

        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { url ->
                val material = Material(id, title, url.toString(), System.currentTimeMillis())
                dbRef.child(courseId).child(id).setValue(material).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Material uploaded", Toast.LENGTH_SHORT).show()
                    loadMaterials()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadMaterial(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun deleteMaterial(material: Material) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Material")
            .setMessage("Delete ${material.title}?")
            .setPositiveButton("Yes") { _, _ ->
                dbRef.child(courseId).child(material.id).removeValue().addOnSuccessListener {
                    Toast.makeText(requireContext(), "Material deleted", Toast.LENGTH_SHORT).show()
                    loadMaterials()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            Toast.makeText(requireContext(), "File Selected", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    }

