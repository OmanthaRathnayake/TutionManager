package com.example.tutionmanager

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AttendanceFragment : Fragment() {
    private lateinit var btnScan: Button
    private lateinit var courseId: String

    companion object {
        fun newInstance(courseId: String): AttendanceFragment {
            val fragment = AttendanceFragment()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_attendance, container, false)
        btnScan = view.findViewById(R.id.btnScanQR)

        btnScan.setOnClickListener {
            val integrator = IntentIntegrator.forSupportFragment(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Scan Student QR")
            integrator.initiateScan()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val studentId = result.contents
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            FirebaseDatabase.getInstance().getReference("attendance")
                .child(courseId).child(studentId).child(date)
                .setValue("present")
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}