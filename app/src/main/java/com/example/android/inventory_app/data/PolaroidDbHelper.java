package com.example.android.inventory_app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventory_app.data.PolaroidContract.PolaroidEntry;

/**
 * Created by evi on 14. 7. 2017.
 */

public class PolaroidDbHelper extends SQLiteOpenHelper

    {

    public static final String LOG_TAG = PolaroidDbHelper.class.getSimpleName();

    // database file
    private static final String DATABASE_NAME = "cameras.db";


    //Database version.
    private static final int DATABASE_VERSION = 1;

    // Constructs a new instance of {@link PolaroidDbHelper}.

    public PolaroidDbHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


     // This is called when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_POLAROID_TABLE =  "CREATE TABLE " + PolaroidEntry.TABLE_NAME + " ("
                + PolaroidEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PolaroidEntry.COLUMN_POLAROID_NAME + " TEXT NOT NULL, "
                + PolaroidEntry.COLUMN_POLAROID_QTY + " INTEGER NOT NULL, "
                + PolaroidEntry.COLUMN_POLAROID_PRICE + " REAL, "
                + PolaroidEntry.COLUMN_POLAROID_SUPPLIER + " TEXT, "
                + PolaroidEntry.COLUMN_POLAROID_PICTURE + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_POLAROID_TABLE);
    }

    //This is called when the database needs to be upgraded. Not used here

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
