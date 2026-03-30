package com.example.bandly30

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegistrationActivity : AppCompatActivity() {
    private lateinit var dbHelper: SQLHelper

    // Полный список из 30 городов
    private val cities = arrayOf(
        "Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург", "Казань",
        "Нижний Новгород", "Челябинск", "Самара", "Омск", "Ростов-на-Дону",
        "Уфа", "Красноярск", "Воронеж", "Пермь", "Волгоград",
        "Краснодар", "Саратов", "Тюмень", "Тольятти", "Ижевск",
        "Барнаул", "Ульяновск", "Иркутск", "Хабаровск", "Махачкала",
        "Владивосток", "Ярославль", "Оренбург", "Томск", "Кемерово"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        dbHelper = SQLHelper(this)

        // Настройка выпадающего списка городов
        val cityInput = findViewById<AutoCompleteTextView>(R.id.placeAutoComplete)
        cityInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities))
        cityInput.setOnClickListener { cityInput.showDropDown() }

        // Находим кнопку регистрации
        findViewById<ImageButton>(R.id.loginbtn2).setOnClickListener {
            // Считываем данные из всех полей
            val name = findViewById<EditText>(R.id.inputName).text.toString().trim()
            val ageStr = findViewById<EditText>(R.id.inputAge).text.toString().trim() // Считываем возраст
            val phone = findViewById<EditText>(R.id.inputPhoneLogin).text.toString().trim()
            val pass = findViewById<EditText>(R.id.inputPassLogin).text.toString().trim()
            val city = cityInput.text.toString().trim()

            // Проверка на пустые поля
            if (phone.isEmpty() || name.isEmpty() || pass.isEmpty() || ageStr.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Переводим возраст в число
            val age = ageStr.toIntOrNull() ?: 0

            val db = dbHelper.writableDatabase

            // Проверка: не занят ли номер телефона
            val cursor = db.query(SQLHelper.TABLE, null, "${SQLHelper.COLUMN_PHONE}=?", arrayOf(phone), null, null, null)
            if (cursor.count > 0) {
                Toast.makeText(this, "Этот номер уже зарегистрирован", Toast.LENGTH_SHORT).show()
                cursor.close()
                return@setOnClickListener
            }
            cursor.close()

            // СОХРАНЕНИЕ В БАЗУ
            val values = ContentValues().apply {
                put(SQLHelper.COLUMN_NAME, name)
                put(SQLHelper.COLUMN_AGE, age)       // ТЕПЕРЬ ВОЗРАСТ СОХРАНЯЕТСЯ
                put(SQLHelper.COLUMN_PHONE, phone)
                put(SQLHelper.COLUMN_PASSWORD, pass)
                put(SQLHelper.COLUMN_CITY, city)
            }

            if (db.insert(SQLHelper.TABLE, null, values) != -1L) {
                // Сохраняем сессию в SharedPreferences
                getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("USER_PHONE", phone)
                    .apply()

                Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()

                // Переход в редактирование профиля
                val intent = Intent(this, EditProfileActivity::class.java)
                intent.putExtra("USER_PHONE", phone)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Ошибка при записи в базу", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
