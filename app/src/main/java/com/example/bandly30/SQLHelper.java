package com.example.bandly30;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Bandly.db";

    private static final int SCHEMA = 9;

    public static final String TABLE = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_INSTRUMENTS = "instruments";
    public static final String COLUMN_GENRES = "genres";
    public static final String COLUMN_BIO = "bio";
    public static final String COLUMN_AVATAR = "avatar";

    // Константы для таблицы лайков (кто кого лайкнул)
    public static final String TABLE_LIKES = "likes";
    public static final String COLUMN_LIKE_ID = "l_id";
    public static final String COLUMN_WHO_PHONE = "who_phone";   // Номер того, кто ставит лайк
    public static final String COLUMN_WHOM_PHONE = "whom_phone"; // Номер того, кого лайкают

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // таблица для пользователей
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME + " TEXT, " +
                COLUMN_AGE + " INTEGER, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_PHONE + " TEXT UNIQUE, " +
                COLUMN_CITY + " TEXT, " +
                COLUMN_INSTRUMENTS + " TEXT, " +
                COLUMN_GENRES + " TEXT, " +
                COLUMN_BIO + " TEXT, " +
                COLUMN_AVATAR + " TEXT);");

        // таблица для лайков
        db.execSQL("CREATE TABLE " + TABLE_LIKES + " (" +
                COLUMN_LIKE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_WHO_PHONE + " TEXT, " +
                COLUMN_WHOM_PHONE + " TEXT);");
    }

    // снос аккаунтов при новой версии бд
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKES);
        onCreate(db);
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.delete(TABLE_LIKES, null, null);
    }

    public long addUser(String name, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues(); // Контейнер для передачи данных в БД
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_PHONE, phone);
        cv.put(COLUMN_PASSWORD, password);
        return db.insert(TABLE, null, cv);
    }

    // удаление аккаунта самим владельцем
    public void deleteUser(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Удаляем лайки, где участвует этот телефон
        db.delete(TABLE_LIKES, COLUMN_WHO_PHONE + "=? OR " + COLUMN_WHOM_PHONE + "=?", new String[]{phone, phone});
        // Удаляем саму запись пользователя
        db.delete(TABLE, COLUMN_PHONE + "=?", new String[]{phone});
    }

    public Cursor getLikersFullData(String myPhone) {
        SQLiteDatabase db = this.getReadableDatabase();
        // JOIN объединяет таблицу лайков с таблицей пользователей для получения полной анкеты
        String query = "SELECT u." + COLUMN_NAME + ", u." + COLUMN_AGE + ", u." + COLUMN_PHONE + ", u." + COLUMN_AVATAR +
                " FROM " + TABLE + " u " +
                " JOIN " + TABLE_LIKES + " l ON u." + COLUMN_PHONE + " = l." + COLUMN_WHO_PHONE +
                " WHERE l." + COLUMN_WHOM_PHONE + " = ?";
        return db.rawQuery(query, new String[]{myPhone});
    }

    public Cursor getPotentialMatches(String myPhone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE +
                " WHERE " + COLUMN_PHONE + " != ? " +
                " AND " + COLUMN_PHONE + " NOT IN (SELECT " + COLUMN_WHOM_PHONE + " FROM " + TABLE_LIKES + " WHERE " + COLUMN_WHO_PHONE + " = ?)";
        return db.rawQuery(query, new String[]{myPhone, myPhone});
    }

 // авторизация
    public boolean checkUser(String phone, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE + " WHERE " + COLUMN_PHONE + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{phone, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    //  проверка лайков
    public boolean isMatch(String myPhone, String otherPhone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_LIKES + " WHERE " + COLUMN_WHO_PHONE + "=? AND " + COLUMN_WHOM_PHONE + "=? " +
                "AND EXISTS (SELECT 1 FROM " + TABLE_LIKES + " WHERE " + COLUMN_WHO_PHONE + "=? AND " + COLUMN_WHOM_PHONE + "=?)";
        Cursor cursor = db.rawQuery(query, new String[]{myPhone, otherPhone, otherPhone, myPhone});
        boolean match = cursor.getCount() > 0;
        cursor.close();
        return match;
    }
}