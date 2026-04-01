package com.example.bandly30

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import java.io.File

class HomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLHelper
    private var usersCursor: Cursor? = null
    private var myPhone: String? = null
    private var currentOtherPhone: String? = null

    private lateinit var cardContainer: View

    private var dX = 0f
    private var initialX = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        dbHelper = SQLHelper(this)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        myPhone = sharedPref.getString("USER_PHONE", "")

        cardContainer = findViewById(R.id.swipeCardContainer)

        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usersCursor = dbHelper.getPotentialMatches(myPhone ?: "")

        val btnLike = findViewById<ImageButton>(R.id.btnNext)
        val btnDislike = findViewById<ImageButton>(R.id.backbtn)

        showNextUser()

        btnLike.setOnClickListener { performSwipeAction(isLike = true) }
        btnDislike.setOnClickListener { performSwipeAction(isLike = false) }

        cardContainer.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    initialX = view.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    view.x = newX
                    view.rotation = (newX - initialX) / 20f
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diff = view.x - initialX
                    if (Math.abs(diff) > 300) {
                        performSwipeAction(isLike = diff > 0)
                    } else {
                        view.animate().translationX(0f).rotation(0f).setDuration(200).start()
                    }
                    true
                }
                else -> false
            }
        }

        findViewById<ImageButton>(R.id.btnNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.gear).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun performSwipeAction(isLike: Boolean) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val targetX = if (isLike) screenWidth * 1.5f else -screenWidth * 1.5f
        val rotationAngle = if (isLike) 30f else -30f

        cardContainer.animate()
            .translationX(targetX)
            .rotation(rotationAngle)
            .alpha(0f)
            .setDuration(400)
            .withEndAction {
                if (isLike) saveLike()
                showNextUser()
                cardContainer.translationX = 0f
                cardContainer.rotation = 0f
                cardContainer.alpha = 0f
                cardContainer.animate().alpha(1f).setDuration(200).start()
            }
            .start()
    }

    private fun showNextUser() {
        val cursor = usersCursor ?: return
        if (cursor.moveToNext()) {
            displayUserData(cursor)
        } else {
            Toast.makeText(this, "Анкеты закончились!", Toast.LENGTH_SHORT).show()
            findViewById<TextView>(R.id.ProfileName).text = "Это все :("
            findViewById<TextView>(R.id.ProfileCity).text = ""
            findViewById<TextView>(R.id.ProfileBio).text = ""
            findViewById<ImageView>(R.id.profileAvatar).setImageResource(R.drawable.ic_avatar)
            cardContainer.setOnTouchListener(null)
        }
    }

    private fun displayUserData(cursor: Cursor) {
        val name = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_NAME))
        val age = cursor.getInt(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AGE))
        val city = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_CITY))
        val avatar = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_AVATAR))
        val bio = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_BIO))
        currentOtherPhone = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.COLUMN_PHONE))

        findViewById<TextView>(R.id.ProfileName).text = "$name, $age"
        findViewById<TextView>(R.id.ProfileCity).text = city

        val bioTv = findViewById<TextView>(R.id.ProfileBio)
        bioTv.text = if (!bio.isNullOrEmpty()) bio else "Нет описания"

        val avatarImg = findViewById<ImageView>(R.id.profileAvatar)
        if (!avatar.isNullOrEmpty()) {
            val file = File(avatar)
            if (file.exists()) {
                Glide.with(this).load(file).centerCrop().into(avatarImg)
            } else {
                avatarImg.setImageResource(R.drawable.ic_avatar)
            }
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