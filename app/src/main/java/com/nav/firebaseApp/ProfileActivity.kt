package com.nav.firebaseApp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nav.firebaseApp.databinding.ActivityProfileBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var selectedImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userName = ""
    private var name = ""
    private var email = ""
    private var bio = ""
    private var profileImageUrl = ""
    private var isEdit: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserData()

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivAvtar.setOnClickListener {
            showImagePickerOptions()
        }

        binding.saveBtn.setOnClickListener {
            if(isEdit)
                edit()
             else
                 saveUserProfile()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if(currentUser != null) {
            val userId = currentUser.uid
            val userRef = firestore.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document.exists()) {
                        userName = document.getString("username") ?: ""
                        name = document.getString("name") ?: ""
                        bio = document.getString("bio") ?: ""
                        email = document.getString("email") ?: ""
                        email = document.getString("email") ?: ""
                        profileImageUrl = document.getString("profileImageUrl") ?: ""

                        updateUI()

                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load user profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun updateUI() {
        Glide.with(this)
            .load(profileImageUrl)
            .circleCrop()
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(binding.ivAvtar)

        if (userName.isNotEmpty()) {
            binding.userNameRl.visibility = View.VISIBLE
            binding.userNameTv.text = userName
        } else {
            binding.userNameRl.visibility = View.GONE
        }

        if (email.isNotEmpty()) {
            binding.emailRl.visibility = View.VISIBLE
            binding.emaildetailTv.text = email
        } else {
            binding.emailRl.visibility = View.GONE
        }

        binding.userNameEt.setText(name)
        binding.bioEt.setText(bio)
    }

    private fun saveUserProfile() {
        val currentUser = auth.currentUser
        if(currentUser != null) {
            val userId = currentUser.uid
            bio = binding.bioEt.text.toString().trim()
            name = binding.userNameEt.text.toString().trim()
            binding.progressBarAvtar.visibility = View.VISIBLE

            if(bio.isEmpty()) {
                Toast.makeText(this, "Please enter a bip", Toast.LENGTH_SHORT).show()
                return
            } else if(name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return
            }

            val userRef = firestore.collection("users").document(userId)
            val profileData = hashMapOf(
                "name" to name,
                "bio" to bio,
                "profileImageUrl" to selectedImageUri.toString()
            )

            userRef.set(profileData)
                .addOnSuccessListener {
                    save()
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
                .addOnCompleteListener {
                    binding.progressBarAvtar.visibility = View.GONE
                }
        }
    }

    private fun save() {
        binding.userNameEt.setText(name)
        binding.bioEt.setText(bio)
        binding.saveBtn.text = getString(R.string.edit)
        binding.userNameEt.isEnabled = false
        binding.bioEt.isEnabled = false
        isEdit = true
        binding.ivAvtar.isEnabled = false
        binding.rlEditImage.visibility = View.GONE
    }

    private fun edit() {
        binding.saveBtn.text = getString(R.string.save)
        binding.userNameEt.isEnabled = true
        binding.bioEt.isEnabled = true
        isEdit = false
        binding.ivAvtar.isEnabled = true
        binding.rlEditImage.visibility = View.VISIBLE
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Gallery", "Camera")
        AlertDialog.Builder(this)
            .setTitle("Choose Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openGallery() {
        val permission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_EXTERNAL_STORAGE
        else
            Manifest.permission.READ_MEDIA_IMAGES

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
        }
    }

    private fun openCamera() {
        val permissions = mutableListOf<String>()
        val storagePermission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        else
            Manifest.permission.READ_MEDIA_IMAGES

        permissions.add(storagePermission)
        permissions.add(Manifest.permission.CAMERA)

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, 100)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        }
    }


    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                val uri = it.data
                uri?.let {
                    selectedImageUri = uri
                    loadImage(uri)
                }
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val photo = data!!.extras!!["data"] as Bitmap?
            selectedImageUri = getImageUri(this, photo!!)
            loadImage(selectedImageUri!!)
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.getContentResolver(),
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }


    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .apply(RequestOptions().placeholder(R.drawable.ic_profile_placeholder))
            .into(binding.ivAvtar)
    }
}