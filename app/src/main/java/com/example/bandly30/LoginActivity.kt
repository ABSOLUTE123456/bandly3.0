package com.example.bandly30

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = SQLHelper(this)

        val phoneInput = findViewById<EditText>(R.id.inputPhoneLogin)
        val passInput = findViewById<EditText>(R.id.inputPassLogin)
        val loginBtn = findViewById<ImageButton>(R.id.loginbtn2)

        loginBtn.setOnClickListener {
            val phone = phoneInput.text.toString().trim()
            val password = passInput.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите номер и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверяем пользователя в БД через метод, который мы создали в SQLHelper
            val isUserExist = dbHelper.checkUser(phone, password)

            if (isUserExist) {
                // 1. СОХРАНЯЕМ СЕССИЮ (номер телефона)
                // Без этого ProfileActivity не поймет, чьи данные загружать
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                sharedPref.edit().putString("USER_PHONE", phone).apply()

                Toast.makeText(this, "Вход выполнен!", Toast.LENGTH_SHORT).show()

                // 2. ПЕРЕХОДИМ В ПРОФИЛЬ
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("USER_PHONE", phone) // На всякий случай дублируем в Intent
                startActivity(intent)
                finish() // Закрываем экран логина
            } else {
                Toast.makeText(this, "Неверный номер или пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
