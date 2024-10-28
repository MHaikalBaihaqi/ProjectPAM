package com.example.projectpam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    private val itemList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        myAdapter = MyAdapter(itemList)
        recyclerView.adapter = myAdapter

        database = FirebaseDatabase.getInstance("https://projectpam-36a7c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("images")
        fetchItems()

        findViewById<Button>(R.id.openImageUploadButton).setOnClickListener {
            startActivity(Intent(this, ImageUploadActivity::class.java))
        }
    }

    private fun fetchItems() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (data in snapshot.children) {
                    val imageUrl = data.getValue(String::class.java)
                    imageUrl?.let { itemList.add(it) }
                }
                myAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load images", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
