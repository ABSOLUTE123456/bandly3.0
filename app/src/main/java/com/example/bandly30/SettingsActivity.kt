package com.example.bandly30

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        dbHelper = SQLHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.logout).setOnClickListener {
            performLogout()
        }

        findViewById<ImageButton>(R.id.delete).setOnClickListener {
            showDeleteDialog()
        }

        findViewById<ImageButton>(R.id.profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<ImageButton>(R.id.backbtn).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<ImageButton>(R.id.about).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        findViewById<ImageButton>(R.id.alert).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
    }

    // МЕТОД ДЛЯ ВЫХОДА
    private fun performLogout() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Удаление аккаунта")
            .setMessage("Вы уверены? Все ваши данные и мэтчи будут удалены навсегда.")
            .setPositiveButton("Удалить") { _, _ ->
                performDeletion()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun performDeletion() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val myPhone = sharedPref.getString("USER_PHONE", null)

        if (myPhone != null) {
            dbHelper.deleteUser(myPhone)
            sharedPref.edit().clear().apply()
            Toast.makeText(this, "Аккаунт удален", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
