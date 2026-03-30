package com.example.bandly30;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Bandly.db";
    private static final int SCHEMA = 8; // Подняли версию для обновления

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

    public static final String TABLE_LIKES = "likes";
    public static final String COLUMN_LIKE_ID = "l_id";
    public static final String COLUMN_WHO_PHONE = "who_phone";
    public static final String COLUMN_WHOM_PHONE = "whom_phone";

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME + " TEXT, " +
                COLUMN_AGE + " INTEGER, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_CITY + " TEXT, " +
                COLUMN_INSTRUMENTS + " TEXT, " +
                COLUMN_GENRES + " TEXT, " +
                COLUMN_BIO + " TEXT, " +
                COLUMN_AVATAR + " TEXT);");

        db.execSQL("CREATE TABLE " + TABLE_LIKES + " (" +
                COLUMN_LIKE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_WHO_PHONE + " TEXT, " +
                COLUMN_WHOM_PHONE + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKES);
        onCreate(db);
    }

    // --- НОВЫЙ МЕТОД ДЛЯ УВЕДОМЛЕНИЙ ---
    // Вытягивает имя, возраст и телефон тех, кто лайкнул текущего юзера
    public Cursor getLikersFullData(String myPhone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u." + COLUMN_NAME + ", u." + COLUMN_AGE + ", u." + COLUMN_PHONE +
                " FROM " + TABLE + " u " +
                " JOIN " + TABLE_LIKES + " l ON u." + COLUMN_PHONE + " = l." + COLUMN_WHO_PHONE +
                " WHERE l." + COLUMN_WHOM_PHONE + " = ?";
        return db.rawQuery(query, new String[]{myPhone});
    }

    // --- ОБНОВЛЕННЫЙ МЕТОД ДЛЯ ЛЕНТЫ (HOME) ---
    // Показывает только тех, кого мы еще НЕ лайкали
    public Cursor getPotentialMatches(String myPhone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE +
                " WHERE " + COLUMN_PHONE + " != ? " +
                " AND " + COLUMN_PHONE + " NOT IN (SELECT " + COLUMN_WHOM_PHONE + " FROM " + TABLE_LIKES + " WHERE " + COLUMN_WHO_PHONE + " = ?)";
        return db.rawQuery(query, new String[]{myPhone, myPhone});
    }

    public boolean checkUser(String phone, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE + " WHERE " + COLUMN_PHONE + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{phone, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

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
