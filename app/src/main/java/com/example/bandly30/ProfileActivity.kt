package com.example.bandly30

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLHelper
    private var userPhone: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = SQLHelper(this)

        // Достаем сохраненный телефон
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userPhone = sharedPref.getString("USER_PHONE", null)

        // Связываем элементы по твоим ID из XML
        val avatarImg = findViewById<ImageView>(R.id.profileAvatar)
        val nameAgeTxt = findViewById<TextView>(R.id.ProfileName)
        val cityTxt = findViewById<TextView>(R.id.ProfileCity)
        val infoTxt = findViewById<TextView>(R.id.tvProfileInfo)
        val bioTxt = findViewById<TextView>(R.id.tvProfileBio)

        loadProfile(avatarImg, nameAgeTxt, cityTxt, infoTxt, bioTxt)

        // Навигация
        findViewById<ImageButton>(R.id.gear).setOnClickListener {
            // Переход на редактирование
        }
        findViewById<ImageButton>(R.id.backbtn).setOnClickListener { finish() }
    }



    private fun loadProfile(avatar: ImageView, nameAge: TextView, city: TextView, info: TextView, bio: TextView) {
        if (userPhone == null) return

        val db = dbHelper.readableDatabase
        val cursor = db.query(SQLHelper.TABLE, null, "${SQLHelper.COLUMN_PHONE} = ?", arrayOf(userPhone), null, null, null)

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_NAME))
            // Достаем возраст. Если в БД он 0, проверь RegistrationActivity (см. ниже)
            val age = cursor.getInt(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AGE))
            val dbCity = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_CITY))
            val inst = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_INSTRUMENTS)) ?: ""
            val gen = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_GENRES)) ?: ""
            val dbBio = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_BIO)) ?: ""
            val avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AVATAR))

            // Устанавливаем данные
            nameAge.text = "$name, $age"
            city.text = dbCity
            info.text = "Инструменты: $inst\nЖанры: $gen"
            bio.text = if (dbBio.isEmpty()) "О себе пока ничего нет..." else dbBio

            // Фото
            if (!avatarPath.isNullOrEmpty()) {
                avatar.setImageURI(Uri.fromFile(File(avatarPath)))
                avatar.clipToOutline = true
                avatar.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
            }
        }
        cursor.close()

        findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<ImageButton>(R.id.gear).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
