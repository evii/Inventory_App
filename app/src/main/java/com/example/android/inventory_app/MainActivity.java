package com.example.android.inventory_app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory_app.data.PolaroidDbHelper;
import com.example.android.inventory_app.data.PolaroidContract.PolaroidEntry;
import com.example.android.inventory_app.data.PolaroidProvider;

import static android.drm.DrmStore.DrmObjectType.CONTENT;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int POLAROID_LOADER = 0;
    PolaroidCursorAdapter mCursorAdapter;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find ListView to populate
        ListView lvItems = (ListView) findViewById(R.id.list_view_polaroid);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        lvItems.setEmptyView(emptyView);

        // Setup cursor adapter using cursor from last step
        mCursorAdapter = new PolaroidCursorAdapter(this, null);

        // Attach cursor adapter to the ListView
        lvItems.setAdapter(mCursorAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(POLAROID_LOADER, null, this);

        //Setup item click listener
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Create new intent to go to Editor Activity
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                // from the uri get the id
                Uri currentPolaroidUri = ContentUris.withAppendedId(PolaroidEntry.CONTENT_URI, id);
                // set the uri on the data field of the intent
                intent.setData(currentPolaroidUri);
                //launch the editor activity to display data for the current polaroid
                startActivity(intent);
            }
        });
    }

    private void insertPolaroid() {

// Create a ContentValues object where column names are the keys,
// and dummy Polaroid attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PolaroidEntry.COLUMN_POLAROID_NAME, "Polaroid SX-70 Alpha");
        values.put(PolaroidEntry.COLUMN_POLAROID_QTY, 5);
        values.put(PolaroidEntry.COLUMN_POLAROID_PRICE, 3990);
        values.put(PolaroidEntry.COLUMN_POLAROID_SUPPLIER, "supply@polaroid.com");
        values.put(PolaroidEntry.COLUMN_POLAROID_PICTURE, "content://com.android.providers.media.documents/document/image%3A47343");

// Insert the new row, returning the primary key value of the new row
        Uri newUri = getContentResolver().insert(PolaroidEntry.CONTENT_URI, values);
        Log.v(LOG_TAG, "New URI: " + newUri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPolaroid();

                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPolaroids();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.

        //Define Projection - columns we are interested in
        String[] projection = {
                PolaroidEntry._ID,
                PolaroidEntry.COLUMN_POLAROID_NAME,
                PolaroidEntry.COLUMN_POLAROID_QTY,
                PolaroidEntry.COLUMN_POLAROID_PRICE};

        return new CursorLoader(this, PolaroidEntry.CONTENT_URI,
                projection,                            // The columns to return
                null,                                    // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null);                                   //sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This callback is called when data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllPolaroids() {
        int rowsDeleted = getContentResolver().delete(PolaroidEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from polaroid database");
    }
}

