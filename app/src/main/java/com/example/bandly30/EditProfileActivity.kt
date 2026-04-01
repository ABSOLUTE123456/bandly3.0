package com.example.bandly30

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide // ДОБАВЛЕНО
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var dbHelper: SQLHelper
    private var userPhone: String? = null
    private var avatarPath: String? = null

    private val instruments = arrayOf("Гитара", "Барабаны", "Вокал", "Бас", "Пианино")
    private val genres = arrayOf("Рок", "Джаз", "Поп", "Метал", "Рэп")
    private var selInst = mutableListOf<String>()
    private var selGen = mutableListOf<String>()

    // 1. Инициализируем выбор фото с использованием Glide для круглой обрезки
    private val pickImg = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val avatarView = findViewById<ImageView>(R.id.imageView9)
            // Использование Glide для предпросмотра круглой аватарки
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(avatarView)

            saveToInternal(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        dbHelper = SQLHelper(this)
        userPhone = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("USER_PHONE", null)

        val nameEdit = findViewById<EditText>(R.id.editText1)
        val cityEdit = findViewById<AutoCompleteTextView>(R.id.placeAutoComplete)
        val instEdit = findViewById<AutoCompleteTextView>(R.id.placeAutoComplete4)
        val genEdit = findViewById<AutoCompleteTextView>(R.id.genreAutoComplete1)
        val bioEdit = findViewById<EditText>(R.id.editText)
        val avatarView = findViewById<ImageView>(R.id.imageView9)

        // Загрузка данных
        loadExistingData(nameEdit, cityEdit, bioEdit, instEdit, genEdit, avatarView)

        // Кнопка выбора фото
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            pickImg.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        instEdit.setOnClickListener { showMultiDialog("Инструменты", instruments, selInst, instEdit) }
        genEdit.setOnClickListener { showMultiDialog("Жанры", genres, selGen, genEdit) }

        // Сохранение
        findViewById<ImageButton>(R.id.accept).setOnClickListener {
            if (userPhone == null) return@setOnClickListener

            val db = dbHelper.writableDatabase
            val v = ContentValues().apply {
                put(SQLHelper.COLUMN_NAME, nameEdit.text.toString().trim())
                put(SQLHelper.COLUMN_CITY, cityEdit.text.toString().trim())
                put(SQLHelper.COLUMN_BIO, bioEdit.text.toString().trim())
                put(SQLHelper.COLUMN_INSTRUMENTS, selInst.joinToString(", "))
                put(SQLHelper.COLUMN_GENRES, selGen.joinToString(", "))
                avatarPath?.let { put(SQLHelper.COLUMN_AVATAR, it) }
            }

            val result = db.update(SQLHelper.TABLE, v, "${SQLHelper.COLUMN_PHONE}=?", arrayOf(userPhone))

            if (result > 0) {
                Toast.makeText(this, "Данные сохранены!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }

        findViewById<ImageButton>(R.id.backbtn).setOnClickListener { finish() }
    }

    private fun saveToInternal(uri: Uri) {
        try {
            val file = File(filesDir, "avatar_${userPhone}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            avatarPath = file.absolutePath
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка сохранения фото", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExistingData(n: EditText, c: AutoCompleteTextView, b: EditText, iE: AutoCompleteTextView, gE: AutoCompleteTextView, img: ImageView) {
        val db = dbHelper.readableDatabase
        val cur = db.query(SQLHelper.TABLE, null, "${SQLHelper.COLUMN_PHONE}=?", arrayOf(userPhone), null, null, null)

        if (cur.moveToFirst()) {
            n.setText(cur.getString(cur.getColumnIndexOrThrow(SQLHelper.COLUMN_NAME)))
            c.setText(cur.getString(cur.getColumnIndexOrThrow(SQLHelper.COLUMN_CITY)))
            b.setText(cur.getString(cur.getColumnIndexOrThrow(SQLHelper.COLUMN_BIO)))

            val savedPath = cur.getString(cur.getColumnIndexOrThrow(SQLHelper.COLUMN_AVATAR))
            if (!savedPath.isNullOrEmpty()) {
                val file = File(savedPath)
                if (file.exists()) {
                    // Используем Glide для загрузки сохраненного фото по кругу
                    Glide.with(this)
                        .load(file)
                        .circleCrop()
                        .into(img)
                    avatarPath = savedPath
                }
            }

            val sInst = cur.getString(cur.getColumnIndexOrThrow(SQLHelper.COLUMN_INSTRUMENTS)) ?: ""
            if (sInst.isNotEmpty()) {
                selInst = sInst.split(", ").toMutableList()
                iE.setText(sInst)
            }

            val sGen = cur.getString(cur.getColumnIndexOrThrow(SQLHelper.COLUMN_GENRES)) ?: ""
            if (sGen.isNotEmpty()) {
                selGen = sGen.split(", ").toMutableList()
                gE.setText(sGen)
            }
        }
        cur.close()
    }

    private fun showMultiDialog(t: String, items: Array<String>, sel: MutableList<String>, out: AutoCompleteTextView) {
        val checked = BooleanArray(items.size) { sel.contains(items[it]) }
        AlertDialog.Builder(this)
            .setTitle(t)
            .setMultiChoiceItems(items, checked) { _, i, isC ->
                if (isC) { if (!sel.contains(items[i])) sel.add(items[i]) }
                else { sel.remove(items[i]) }
            }
            .setPositiveButton("OK") { _, _ -> out.setText(sel.joinToString(", ")) }
            .show()
    }
}
