package com.example.bandly30

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private val pickImg = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            findViewById<ImageView>(R.id.imageView9).setImageURI(it)
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

        // Загрузка существующих данных
        loadData(nameEdit, cityEdit, bioEdit)

        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                pickImg.launch("image/*")
            } else {
                registerForActivityResult(ActivityResultContracts.RequestPermission()){}.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        instEdit.setOnClickListener { showMultiDialog("Инструменты", instruments, selInst, instEdit) }
        genEdit.setOnClickListener { showMultiDialog("Жанры", genres, selGen, genEdit) }

        findViewById<ImageButton>(R.id.imageButton2).setOnClickListener {
            val db = dbHelper.writableDatabase
            val v = ContentValues().apply {
                put(SQLHelper.COLUMN_NAME, nameEdit.text.toString())
                put(SQLHelper.COLUMN_CITY, cityEdit.text.toString())
                put(SQLHelper.COLUMN_BIO, bioEdit.text.toString())
                put(SQLHelper.COLUMN_INSTRUMENTS, selInst.joinToString(", "))
                put(SQLHelper.COLUMN_GENRES, selGen.joinToString(", "))
                put(SQLHelper.COLUMN_AVATAR, avatarPath)
            }
            db.update(SQLHelper.TABLE, v, "${SQLHelper.COLUMN_PHONE}=?", arrayOf(userPhone))
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun loadData(n: EditText, c: AutoCompleteTextView, b: EditText) {
        val db = dbHelper.readableDatabase
        val cur = db.query(SQLHelper.TABLE, null, "${SQLHelper.COLUMN_PHONE}=?", arrayOf(userPhone), null, null, null)
        if (cur.moveToFirst()) {
            n.setText(cur.getString(cur.getColumnIndexOrThrow(SQLHelper.COLUMN_NAME)))
            c.setText(cur.getString(cur.getColumnIndexOrThrow(SQLHelper.COLUMN_CITY)))
            b.setText(cur.getString(cur.getColumnIndexOrThrow(SQLHelper.COLUMN_BIO)))
        }
        cur.close()
    }

    private fun showMultiDialog(t: String, items: Array<String>, sel: MutableList<String>, out: AutoCompleteTextView) {
        val checked = BooleanArray(items.size) { sel.contains(items[it]) }
        AlertDialog.Builder(this).setTitle(t).setMultiChoiceItems(items, checked) { _, i, isC ->
            if (isC) sel.add(items[i]) else sel.remove(items[i])
        }.setPositiveButton("OK") { _, _ -> out.setText(sel.joinToString(", ")) }.show()
    }

    private fun saveToInternal(uri: Uri) {
        val file = File(filesDir, "avatar_$userPhone.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        avatarPath = file.absolutePath
    }
}
