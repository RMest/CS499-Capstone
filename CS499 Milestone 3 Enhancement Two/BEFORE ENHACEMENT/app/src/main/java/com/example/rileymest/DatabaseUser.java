package com.example.rileymest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseUser extends SQLiteOpenHelper {

    // initializations
    private static final String DATABASE_NAME = "users.db";
    private static final int VERSION = 1;

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
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        db.execSQL("drop table if exists " + UserTable.TABLE);
        onCreate(db);
    }

    public long addUser(String username, String password) { // adds user to database
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(UserTable.USERNAME, username);
        values.put(UserTable.PASSWORD, password);

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
                UserTable.TABLE,
                new String[]{UserTable.ID},
                UserTable.USERNAME + "=? AND " + UserTable.PASSWORD + "=?",
                new String[]{username, password},
                null, null, null, "1")) {
            return c.moveToFirst();
        }
    }
    
}