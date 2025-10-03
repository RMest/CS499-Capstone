package com.example.rileymest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseInventory extends SQLiteOpenHelper {

    // initializations
    private static final String DATABASE_NAME = "inventory.db";
    private static final int VERSION = 1;

    public DatabaseInventory(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final class InventoryTable {
        private static final String TABLE = "Inventory";
        private static final String ID = "_id";
        private static final String ITEM = "item";
        private static final String QUANTITY = "qty";
    }

    @Override
    public void onCreate(SQLiteDatabase db) { // ran when running java file
        db.execSQL("create table if not exists " + InventoryTable.TABLE + " (" +
                InventoryTable.ID + " integer primary key autoincrement, " +
                InventoryTable.ITEM + " text not null unique collate nocase, " +
                InventoryTable.QUANTITY + " integer not null default 0 CHECK(" + InventoryTable.QUANTITY + " >= 0)" + ")");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        db.execSQL("drop table if exists " + InventoryTable.TABLE);
        onCreate(db);
    }

    public Cursor getAll() { // gets whole database
        SQLiteDatabase db = getReadableDatabase();
        return db.query(InventoryTable.TABLE, null, null, null, null, null,
                InventoryTable.ITEM + " ASC");
    }

    public long addItem(String item, int qty) { // adds item to database
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(InventoryTable.ITEM, item);
        values.put(InventoryTable.QUANTITY, qty);

        return db.insert(InventoryTable.TABLE, null, values);
    }

    public void updateQuantity(long id, int newQty) { // updates quantities
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InventoryTable.QUANTITY, newQty);
        db.update(InventoryTable.TABLE,
                values, InventoryTable.ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public void deleteById(long id) { // deletes item
        SQLiteDatabase db = getWritableDatabase();
        db.delete(InventoryTable.TABLE,
                InventoryTable.ID + "=?",
                new String[]{String.valueOf(id)});
    }
}
