package com.example.tutionmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class StudentProfileFragment : Fragment() {
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var qrImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_profile, container, false)
        tvName = view.findViewById(R.id.tvStudentName)
        tvEmail = view.findViewById(R.id.tvStudentEmail)
        qrImageView = view.findViewById(R.id.qrImageView)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return view
        loadStudentData(userId)

        return view
    }

    private fun loadStudentData(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").value?.toString() ?: "N/A"
                val email = snapshot.child("email").value?.toString() ?: "N/A"
                val qrUrl = snapshot.child("qrCodeUrl").value?.toString() ?: ""

                tvName.text = name
                tvEmail.text = email

                if (qrUrl.isNotEmpty()) {
                    Glide.with(requireContext()).load(qrUrl).into(qrImageView)
                } else {
                    Toast.makeText(requireContext(), "No QR code found", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
        }
    }
    }

