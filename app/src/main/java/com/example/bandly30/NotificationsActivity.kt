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

        // Список для отображения в ListView (имя + возраст + номер)
        val displayList = mutableListOf<String>()
        // Список только с номерами телефонов (для открытия профиля при клике)
        val phoneList = mutableListOf<String>()

        // 1. Используем новый метод с JOIN для получения всех данных
        val cursor = dbHelper.getLikersFullData(myPhone)

        if (cursor.moveToFirst()) {
            do {
                // Достаем данные из разных колонок
                val name = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_NAME))
                val age = cursor.getInt(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AGE))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_PHONE))

                // Формируем красивую строку для пользователя
                displayList.add("Имя: $name, Возраст: $age\nТел: $phone")
                // Сохраняем телефон отдельно
                phoneList.add(phone)
            } while (cursor.moveToNext())
        } else {
            displayList.add("У вас пока нет лайков :(")
        }
        cursor.close()

        // 2. Настройка адаптера
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter

        // 3. Клик по элементу списка
        listView.setOnItemClickListener { _, _, position, _ ->
            // Проверяем, что список не пустой (не сообщение о пустых лайках)
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
