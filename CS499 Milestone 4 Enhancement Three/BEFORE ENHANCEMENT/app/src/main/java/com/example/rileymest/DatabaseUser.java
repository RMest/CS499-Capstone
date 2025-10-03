package com.example.rileymest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.net.PasswordAuthentication;

public class DatabaseUser extends SQLiteOpenHelper {

    // initializations
    private static final String DATABASE_NAME = "users.db";
    private static final int VERSION = 1;
    private String currUserID;

    public DatabaseUser(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final class UserTable {
        private static final String TABLE = "USERS";
        private static final String ID = "_id";
        private static final String USERNAME = "username";
        private static final String PASSWORD = "password";
    }

    @Override
    public void onCreate(SQLiteDatabase db) { // ran when running java file
        db.execSQL("create table if not exists " + UserTable.TABLE + " (" +
                UserTable.ID + " integer primary key autoincrement, " +
                UserTable.USERNAME + " text not null unique, " +
                UserTable.PASSWORD + " text not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + UserTable.TABLE);
        onCreate(db);
    }

    public long addUser(String username, String password) { // adds user to database
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(UserTable.USERNAME, username);
        values.put(UserTable.PASSWORD, encryptDecryptString(password));

        return db.insert(UserTable.TABLE, null, values);
    }

    public boolean userExist(String username) { // ran to check if username already exists
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                UserTable.TABLE,
                new String[]{UserTable.ID},
                UserTable.USERNAME + "=?",
                new String[]{username},
                null, null, null, "1")) {
            return c.moveToFirst();
        }
    }

    public boolean validateLogin(String username, String password) { // checks if user info match database
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                UserTable.TABLE, new String[]{UserTable.ID},
                UserTable.USERNAME + "=? AND " + UserTable.PASSWORD + "=?",
                new String[]{username, encryptDecryptString(password)},
                null, null, null, "1")) {
            return c.moveToFirst();
        }
    }

    public boolean updatePassword(int userId, String oldPassword, String newPassword) { // updates password of current user
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor c = db.query
                (UserTable.TABLE, new String[]{UserTable.PASSWORD},
                UserTable.ID + "=? AND " + UserTable.PASSWORD + "=?",
                new String[]{String.valueOf(userId), encryptDecryptString(oldPassword)},
                null, null, null, "1")) {
            if (!c.moveToFirst()) return false;
        }
        ContentValues v = new ContentValues();
        v.put(UserTable.PASSWORD, encryptDecryptString(newPassword));
        return db.update(UserTable.TABLE, v, UserTable.ID + "=?", new String[]{String.valueOf(userId)}) == 1;
    }

    public int getUserIdByUsername(String username) { // retrieves ID from username
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                "USERS",
                new String[]{"_id"},
                "username=?",
                new String[]{username},
                null, null, null, "1")) {
            if (c.moveToFirst()) return c.getInt(c.getColumnIndexOrThrow("_id"));
        }
        return -1;
    }

    public String getUsernameById(int userId) { // gets username from ID
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                "USERS",
                new String[]{"username"},
                "_id=?",
                new String[]{String.valueOf(userId)},
                null, null, null, "1")) {
            if (c.moveToFirst()) return c.getString(c.getColumnIndexOrThrow("username"));
        }
        return null;
    }

    private String encryptDecryptString(String string) { // simple XOR encryption method
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            String key = "azbxcwdveut";
            char stringChar = string.charAt(i);
            char keyChar = key.charAt(i % key.length());
            stringBuilder.append((char) (stringChar ^ keyChar));
        }
        return stringBuilder.toString();
    }
}