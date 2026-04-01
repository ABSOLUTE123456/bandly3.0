package com.example.bandly30

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class NotificationsActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        dbHelper = SQLHelper(this)

        val listView = findViewById<ListView>(R.id.notificationsList)
        val backBtn = findViewById<ImageButton>(R.id.backBtn)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val myPhone = sharedPref.getString("USER_PHONE", "") ?: ""


        val displayList = mutableListOf<String>()

        val phoneList = mutableListOf<String>()


        val cursor = dbHelper.getLikersFullData(myPhone)

        if (cursor.moveToFirst()) {
            do {

                val name = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_NAME))
                val age = cursor.getInt(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AGE))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_PHONE))


                displayList.add("Имя: $name, Возраст: $age\nТел: $phone")

                phoneList.add(phone)
            } while (cursor.moveToNext())
        } else {
            displayList.add("ВЫ НИКОМУ НЕ НУЖНЫ :(")
        }
        cursor.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            if (phoneList.isNotEmpty()) {
                val selectedPhone = phoneList[position]
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("USER_PHONE", selectedPhone)
                startActivity(intent)
            }
        }

        backBtn.setOnClickListener {
            finish()
        }
    }
}
