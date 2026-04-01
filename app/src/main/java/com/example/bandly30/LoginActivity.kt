package com.example.bandly30

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        phoneInput.addTextChangedListener(PhoneMaskWatcher(phoneInput))

        loginBtn.setOnClickListener {
            val rawPhone = phoneInput.text.toString().replace(Regex("[^\\d]"), "")
            val password = passInput.text.toString().trim()

            if (rawPhone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите номер и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isUserExist = dbHelper.checkUser(rawPhone, password)

            if (isUserExist) {
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                sharedPref.edit().putString("USER_PHONE", rawPhone).apply()

                Toast.makeText(this, "Вход выполнен!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("USER_PHONE", rawPhone)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Неверный номер или пароль", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.backbutton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
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
            editText.setSelection(formatted.length) // Курсор всегда в конце
            isUpdating = false
        }
    }
}