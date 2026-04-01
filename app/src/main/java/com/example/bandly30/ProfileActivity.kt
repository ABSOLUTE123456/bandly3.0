package com.example.bandly30

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLHelper
    private var phoneToShow: String? = null // Тот, кого показываем
    private var myOwnPhone: String? = null   // Мы сами

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = SQLHelper(this)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        myOwnPhone = sharedPref.getString("USER_PHONE", null)

        val intentPhone = intent.getStringExtra("USER_PHONE")
        phoneToShow = intentPhone ?: myOwnPhone

        findViewById<ImageButton>(R.id.gear).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<ImageButton>(R.id.edit1).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<ImageButton>(R.id.backbtn).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        myOwnPhone = sharedPref.getString("USER_PHONE", null)

        if (intent.getStringExtra("USER_PHONE") == null) {
            phoneToShow = myOwnPhone
        }

        refreshUI()
    }

    private fun refreshUI() {
        val avatarImg = findViewById<ImageView>(R.id.profileAvatar)
        val nameAgeTxt = findViewById<TextView>(R.id.ProfileName)
        val cityTxt = findViewById<TextView>(R.id.ProfileCity)
        val infoTxt = findViewById<TextView>(R.id.tvProfileInfo)
        val bioTxt = findViewById<TextView>(R.id.tvProfileBio)
        val gearBtn = findViewById<ImageButton>(R.id.gear)
        val nextBtn = findViewById<ImageButton>(R.id.btnNext)
        val editBtn = findViewById<ImageButton>(R.id.edit1)

        val isNotMe = phoneToShow != null && phoneToShow != myOwnPhone

        if (isNotMe) {
            gearBtn.visibility = View.GONE
            nextBtn.visibility = View.GONE
            editBtn.visibility = View.GONE
        } else {
            gearBtn.visibility = View.VISIBLE
            nextBtn.visibility = View.VISIBLE
            editBtn.visibility = View.VISIBLE
        }

        loadProfile(avatarImg, nameAgeTxt, cityTxt, infoTxt, bioTxt)
    }

    private fun loadProfile(avatar: ImageView, nameAge: TextView, city: TextView, info: TextView, bio: TextView) {
        if (phoneToShow == null || myOwnPhone == null) return

        val db = dbHelper.readableDatabase
        val cursor = db.query(SQLHelper.TABLE, null, "${SQLHelper.COLUMN_PHONE} = ?", arrayOf(phoneToShow), null, null, null)

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_NAME))
            val age = cursor.getInt(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AGE))
            val dbCity = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_CITY))
            val dbPhone = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_PHONE))
            val inst = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_INSTRUMENTS)) ?: ""
            val gen = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_GENRES)) ?: ""
            val dbBio = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_BIO)) ?: ""
            val avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AVATAR))

            nameAge.text = "$name, $age"
            city.text = dbCity
            info.text = "Инструменты: $inst\nЖанры: $gen"
            bio.text = if (dbBio.isEmpty()) "О себе пока ничего нет..." else dbBio

            val phoneTxt = findViewById<TextView>(R.id.tvProfilePhone)
            val isMatch = dbHelper.isMatch(myOwnPhone!!, phoneToShow!!)
            val isItMe = (phoneToShow == myOwnPhone)

            if (isMatch || isItMe) {
                phoneTxt.text = dbPhone
                phoneTxt.visibility = View.VISIBLE
            } else {
                phoneTxt.visibility = View.GONE
            }

            if (!avatarPath.isNullOrEmpty()) {
                val file = File(avatarPath)
                if (file.exists()) {
                    Glide.with(this)
                        .load(file)
                        .centerCrop()
                        .into(avatar)
                } else {
                    avatar.setImageResource(R.drawable.ic_avatar)
                }
            } else {
                avatar.setImageResource(R.drawable.ic_avatar)
            }
        }
        cursor.close()
    }
}
