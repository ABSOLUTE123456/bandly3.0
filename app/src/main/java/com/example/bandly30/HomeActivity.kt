package com.example.bandly30

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class HomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLHelper
    private var usersCursor: Cursor? = null
    private var myPhone: String? = null
    private var currentOtherPhone: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        dbHelper = SQLHelper(this)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        myPhone = sharedPref.getString("USER_PHONE", "")

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // ИСПРАВЛЕНО: используем актуальный метод из SQLHelper
        usersCursor = dbHelper.getPotentialMatches(myPhone ?: "")

        val btnLike = findViewById<ImageButton>(R.id.btnNext)
        val btnDislike = findViewById<ImageButton>(R.id.backbtn)
        val gearBtn = findViewById<ImageButton>(R.id.gear)
        val btnNotify = findViewById<ImageButton>(R.id.btnNotifications)

        showNextUser()

        btnLike.setOnClickListener {
            saveLike()
            showNextUser()
        }

        btnDislike.setOnClickListener {
            showNextUser()
        }

        btnNotify.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        gearBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun showNextUser() {
        // Добавлена проверка на наличие данных в курсоре
        val cursor = usersCursor ?: return

        if (cursor.moveToNext()) {
            displayUserData(cursor)
        } else {
            Toast.makeText(this, "Новых анкет пока нет!", Toast.LENGTH_SHORT).show()
            // Очищаем экран, если анкеты кончились
            findViewById<TextView>(R.id.ProfileName).text = "Это все :("
            findViewById<TextView>(R.id.ProfileCity).text = ""
            findViewById<ImageView>(R.id.profileAvatar).setImageResource(R.drawable.ic_avatar)
        }
    }

    private fun displayUserData(cursor: Cursor) {
        val name = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_NAME))
        val age = cursor.getInt(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AGE))
        val city = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_CITY))
        val avatar = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AVATAR))

        currentOtherPhone = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_PHONE))

        findViewById<TextView>(R.id.ProfileName).text = "$name, $age"
        findViewById<TextView>(R.id.ProfileCity).text = city

        val avatarImg = findViewById<ImageView>(R.id.profileAvatar)
        if (!avatar.isNullOrEmpty()) {
            avatarImg.setImageURI(Uri.fromFile(File(avatar)))
        } else {
            avatarImg.setImageResource(R.drawable.ic_avatar)
        }
    }

    private fun saveLike() {
        if (myPhone == null || currentOtherPhone == null) return

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(SQLHelper.COLUMN_WHO_PHONE, myPhone)
            put(SQLHelper.COLUMN_WHOM_PHONE, currentOtherPhone)
        }

        db.insert(SQLHelper.TABLE_LIKES, null, values)

        if (dbHelper.isMatch(myPhone!!, currentOtherPhone!!)) {
            Toast.makeText(this, "Взаимная симпатия!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        usersCursor?.close()
    }
}
