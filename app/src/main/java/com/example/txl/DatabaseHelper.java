package com.example.txl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_CONTACTS = "contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONES = "phones";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_AVATAR = "avatar";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT NOT NULL,"
                + COLUMN_PHONES + " TEXT NOT NULL,"
                + COLUMN_NOTE + " TEXT,"
                + COLUMN_AVATAR + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COLUMN_AVATAR + " TEXT");
        }
    }

    public long insertContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONES, contact.getPhonesAsString());
        values.put(COLUMN_NOTE, contact.getNote());
        values.put(COLUMN_AVATAR, contact.getAvatarPath());
        long id = db.insert(TABLE_CONTACTS, null, values);
        db.close();
        return id;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Contact contact = cursorToContact(cursor);
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact c1, Contact c2) {
                return c1.getSortLetter().compareTo(c2.getSortLetter());
            }
        });
        return contactList;
    }

    public List<Contact> searchContacts(String query) {
        List<Contact> contactList = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllContacts();
        }
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE "
                + COLUMN_NAME + " LIKE ? OR " + COLUMN_PHONES + " LIKE ?";
        SQLiteDatabase db = this.getReadableDatabase();
        String searchPattern = "%" + query + "%";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{searchPattern, searchPattern});
        if (cursor.moveToFirst()) {
            do {
                Contact contact = cursorToContact(cursor);
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact c1, Contact c2) {
                return c1.getSortLetter().compareTo(c2.getSortLetter());
            }
        });
        return contactList;
    }

    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONES, contact.getPhonesAsString());
        values.put(COLUMN_NOTE, contact.getNote());
        values.put(COLUMN_AVATAR, contact.getAvatarPath());
        int rowsAffected = db.update(TABLE_CONTACTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
        db.close();
        return rowsAffected;
    }

    public void deleteContact(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public Contact getContactById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONES, COLUMN_NOTE, COLUMN_AVATAR},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
        Contact contact = null;
        if (cursor.moveToFirst()) {
            contact = cursorToContact(cursor);
        }
        cursor.close();
        db.close();
        return contact;
    }

    private Contact cursorToContact(Cursor cursor) {
        Contact contact = new Contact();
        contact.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        contact.setPhonesFromString(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONES)));
        contact.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE)));
        contact.setAvatarPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR)));
        contact.setSortLetter(getSortLetter(contact.getName()));
        return contact;
    }

    private String getSortLetter(String name) {
        if (name == null || name.isEmpty()) {
            return "#";
        }
        char firstChar = name.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            return String.valueOf(firstChar);
        } else if (firstChar >= 'a' && firstChar <= 'z') {
            return String.valueOf(Character.toUpperCase(firstChar));
        } else {
            return "#";
        }
    }
}

