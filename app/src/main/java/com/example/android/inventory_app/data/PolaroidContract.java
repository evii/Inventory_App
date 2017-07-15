package com.example.android.inventory_app.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by evi on 14. 7. 2017.
 */

public final class PolaroidContract {

    private PolaroidContract() {
    }

    // "Content authority" - package name
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory_app";

    //create the base of all URI's which apps will use to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // stores the path for each of the tables
    public static final String PATH_POLAROIDS = "polaroids";

    //Inner class that defines constant values for the pets database table.

    public static final class PolaroidEntry implements BaseColumns {

        //The content URI to access the polaroid data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_POLAROIDS);

        //The MIME type of the {@link #CONTENT_URI} for a list of pets.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POLAROIDS;

        //The MIME type of the {@link #CONTENT_URI} for a single pet.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POLAROIDS;

        //Name of database table for pets
        public final static String TABLE_NAME = "polaroids";


        //Unique ID number for the pet (only for use in the database table).
        // Type: INTEGER
        public final static String _ID = BaseColumns._ID;


        // Name of the polaroid.
        //Type: TEXT
        public final static String COLUMN_POLAROID_NAME = "name";


        // Quantity on stock
        // Type: INTEGER
        public final static String COLUMN_POLAROID_QTY = "quanity";

        // Price pf the product
        // Type: REAL
        public final static String COLUMN_POLAROID_PRICE = "price";

        // Supplier contact email
        // Type: TEXT
        public final static String COLUMN_POLAROID_SUPPLIER = "supplier";

        // Picture of the product
        // Type: BLOB
        public final static String COLUMN_POLAROID_PICTURE = "picture";
    }
}
