package com.example.projectpam

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class ImageUploadActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var uploadImageButton: Button
    private var imageUri: Uri? = null
    private lateinit var storageRef: StorageReference
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_upload)

        // Mengatur Toolbar sebagai ActionBar dan mengaktifkan tombol kembali
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageView = findViewById(R.id.imageView)
        selectImageButton = findViewById(R.id.selectImageButton)
        uploadImageButton = findViewById(R.id.uploadImageButton)

        // Inisialisasi Firebase Storage dan Database Reference
        storageRef = FirebaseStorage.getInstance().reference.child("uploads")
        database = FirebaseDatabase.getInstance("https://projectpam-36a7c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("images")

        // Pilih gambar dari galeri
        selectImageButton.setOnClickListener { selectImage() }

        // Upload gambar ke Firebase Storage
        uploadImageButton.setOnClickListener { uploadImage() }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Aksi ketika tombol back di toolbar ditekan
        finish() // Mengembalikan user ke halaman sebelumnya
        return true
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageView.setImageURI(imageUri) // Tampilkan gambar yang dipilih
        }
    }

    private fun uploadImage() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val fileRef = storageRef.child("uploads/${UUID.randomUUID()}.jpg")

        // Mulai proses upload
        fileRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // Tambahkan logging untuk keberhasilan upload
                Toast.makeText(this, "Image uploaded to storage, getting URL...", Toast.LENGTH_SHORT).show()

                // Dapatkan URL download setelah upload berhasil
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Logging URL yang diperoleh
                    Toast.makeText(this, "URL obtained: $downloadUri", Toast.LENGTH_SHORT).show()

                    // Simpan URL ke Realtime Database
                    database.push().setValue(downloadUri.toString())
                        .addOnSuccessListener {
                            // Sukses menyimpan URL ke database
                            Toast.makeText(this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            // Gagal menyimpan URL ke database
                            Toast.makeText(this, "Failed to save URL to database", Toast.LENGTH_SHORT).show()
                        }
                }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to obtain download URL", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
            }
    }

}
