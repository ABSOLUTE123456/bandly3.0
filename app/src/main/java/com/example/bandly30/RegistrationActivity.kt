package com.example.bandly30

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegistrationActivity : AppCompatActivity() {
    private lateinit var dbHelper: SQLHelper

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

        val cityInput = findViewById<AutoCompleteTextView>(R.id.placeAutoComplete)
        val phoneInput = findViewById<EditText>(R.id.inputPhoneLogin)

        phoneInput.addTextChangedListener(PhoneMaskWatcher(phoneInput))

        cityInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities))
        cityInput.setOnClickListener { cityInput.showDropDown() }

        findViewById<ImageButton>(R.id.loginbtn2).setOnClickListener {
            val name = findViewById<EditText>(R.id.inputName).text.toString().trim()
            val ageStr = findViewById<EditText>(R.id.inputAge).text.toString().trim()
            val pass = findViewById<EditText>(R.id.inputPassLogin).text.toString().trim()
            val city = cityInput.text.toString().trim()

            val rawPhone = phoneInput.text.toString().replace(Regex("[^\\d]"), "")

            if (rawPhone.isEmpty() || name.isEmpty() || pass.isEmpty() || ageStr.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rawPhone.length < 11) {
                Toast.makeText(this, "Номер введен не полностью", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageStr.toIntOrNull() ?: 0
            val db = dbHelper.writableDatabase

            val cursor = db.query(SQLHelper.TABLE, null, "${SQLHelper.COLUMN_PHONE}=?", arrayOf(rawPhone), null, null, null)
            if (cursor.count > 0) {
                Toast.makeText(this, "Этот номер уже зарегистрирован", Toast.LENGTH_SHORT).show()
                cursor.close()
                return@setOnClickListener
            }
            cursor.close()

            val values = ContentValues().apply {
                put(SQLHelper.COLUMN_NAME, name)
                put(SQLHelper.COLUMN_AGE, age)
                put(SQLHelper.COLUMN_PHONE, rawPhone)
                put(SQLHelper.COLUMN_PASSWORD, pass)
                put(SQLHelper.COLUMN_CITY, city)
            }

            if (db.insert(SQLHelper.TABLE, null, values) != -1L) {
                getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("USER_PHONE", rawPhone)
                    .apply()

                Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, EditProfileActivity::class.java)
                intent.putExtra("USER_PHONE", rawPhone)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Ошибка при записи в базу", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class PhoneMaskWatcher(private val editText: EditText) : TextWatcher {
        private var isUpdating = false
        private val mask = "+7 (###) ###-##-##"

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isUpdating) return

            var str = s.toString().replace(Regex("[^\\d]"), "")

            if (str.startsWith("7") || str.startsWith("8")) {
                str = str.substring(1)
            }

            val formatted = StringBuilder()
            var i = 0
            for (m in mask.toCharArray()) {
                if (m == '#') {
                    if (i < str.length) {
                        formatted.append(str[i])
                        i++
                    } else break
                } else {
                    if (i < str.length) formatted.append(m)
                }
            }

            isUpdating = true
            editText.setText(formatted.toString())
            editText.setSelection(formatted.length)
            isUpdating = false
        }
    }
}
