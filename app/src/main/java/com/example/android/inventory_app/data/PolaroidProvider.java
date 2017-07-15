package com.example.android.inventory_app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.android.inventory_app.data.PolaroidContract.PolaroidEntry;

import static android.icu.lang.UCharacter.JoiningGroup.PE;

/**
 * Created by evi on 14. 7. 2017.
 */

public class PolaroidProvider extends ContentProvider {
    // database helper object
    private PolaroidDbHelper mDbHelper;

    /** Tag for the log messages */
    public static final String LOG_TAG = PolaroidProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the whole table */
    public static final int POLAROIDS = 100;

    /** URI matcher code for the content URI for a single row in the table */
    public static final int POLAROID_ID = 101;

    /** URI matcher object to match a context URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.inventory_app/polaroids" will map to the
        // integer code {@link #POLAROIDS}. This URI is used to provide access to MULTIPLE rows
        // of the polaroids table.
        sUriMatcher.addURI(PolaroidContract.CONTENT_AUTHORITY, PolaroidContract.PATH_POLAROIDS, POLAROIDS);

        // The content URI of the form "content://com.example.android.inventory_app/polaroids/#" will map to the
        // integer code {@link #POLAROIDS_ID}. This URI is used to provide access to ONE single row
        // of the polaroids table.

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        sUriMatcher.addURI(PolaroidContract.CONTENT_AUTHORITY, PolaroidContract.PATH_POLAROIDS + "/#", POLAROID_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PolaroidDbHelper(getContext());
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        return true;
    }

    //Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case POLAROIDS:
                // query the whole table

                cursor = database.query(PolaroidEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;
            case POLAROID_ID:
                // extract out the ID from the URI.
                // For an example URI such as "com.example.android.inventory_app/polaroids/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PolaroidEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PolaroidEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification uri on the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    //Insert new data into the provider with the given ContentValues.

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POLAROIDS:
                return insertPolaroid(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a polaroid into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPolaroid(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(PolaroidEntry.COLUMN_POLAROID_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Polaroid requires a name");
        }

        // Check that the quantity is not null
        Integer quantity = values.getAsInteger(PolaroidEntry.COLUMN_POLAROID_QTY);
        if (quantity == null ) {
            throw new IllegalArgumentException("Polaroid requires quantity");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(PolaroidEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content uri
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POLAROIDS:
                return updatePolaroid(uri, contentValues, selection, selectionArgs);
            case POLAROID_ID:
                // For the POLAROID_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PolaroidEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePolaroid(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update polaroids in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more polaroids).
     * Return the number of rows that were successfully updated.
     */
    private int updatePolaroid(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the polaroid name key is present,
        // check that the name value is not null.
        if (values.containsKey(PolaroidEntry.COLUMN_POLAROID_NAME)) {
            String name = values.getAsString(PolaroidEntry.COLUMN_POLAROID_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Polaroid requires a name");
            }
        }

        // If the quantity key is present,
        // check that the it is not null.
        if(values.containsKey(PolaroidEntry.COLUMN_POLAROID_QTY)){
            Integer quantity = values.getAsInteger(PolaroidEntry.COLUMN_POLAROID_QTY);
            if (quantity == null) {
                throw new IllegalArgumentException("Polaroid requires quantity");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PolaroidEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }


     //Delete the data at the given selection and selection arguments.

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case POLAROIDS:
                // Track the number of rows that were deleted
                rowsDeleted = database.delete(PolaroidEntry.TABLE_NAME, selection, selectionArgs);
                // Delete all rows that match the selection and selection args

                if(rowsDeleted !=0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                // Delete all rows that match the selection and selection args
                return rowsDeleted;
            case POLAROID_ID:
                // Delete a single row given by the ID in the URI
                selection = PolaroidEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete(PolaroidEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted !=0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POLAROIDS:
                return PolaroidEntry.CONTENT_LIST_TYPE;
            case POLAROID_ID:
                return PolaroidEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
