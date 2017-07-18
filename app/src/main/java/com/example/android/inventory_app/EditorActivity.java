package com.example.android.inventory_app;

/**
 * Created by evi on 15. 7. 2017.
 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.AlertDialog;

import com.example.android.inventory_app.data.PolaroidContract.PolaroidEntry;
import com.example.android.inventory_app.data.PolaroidProvider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.R.attr.data;
import static android.R.id.message;

/**
 * Allows user to create a new polaroid or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //EditText field to enter the polaroid products name
    private EditText mNameEditText;

    //EditText field to enter the quantity of product
    private EditText mQuantityEditText;

    //EditText field to enter the peoduct's price
    private EditText mPriceEditText;

    //EditText field to enter the supplier
    private EditText mSupplierEditText;

    private static final int EXISTING_POLAROID_LOADER = 0;

    // Content URI for the existing polaroid (null if it's a new polaroid)
    private Uri mCurrentPolaroidUri;
    // in editing mode - variable to listen to changes
    private boolean mPolaroidHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are
    // modifying the view, and we change the mPolaroidtHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPolaroidHasChanged = true;
            return false;
        }
    };

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int PICK_IMAGE_REQUEST = 0;

    // Uri of picture
    private Uri mUri;

    ImageView mProductImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Getting the intent from Main Activity,
        // examining it so that we know inserting new polaroid or updating
        Intent intent = getIntent();
        mCurrentPolaroidUri = intent.getData();

        // If the intent does not contatin polaroid uri - we are inserting new polaroid
        if (mCurrentPolaroidUri == null) {
            // change app bar title to Add a polaroid
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a polaroid that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            //otherwise it is an update of existing product
            setTitle(getString(R.string.editor_activity_title_edit_product));
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getSupportLoaderManager().initLoader(EXISTING_POLAROID_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mSupplierEditText = (EditText) findViewById(R.id.edit_supplier_address);
        mProductImageView = (ImageView) findViewById(R.id.product_ImageView);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mProductImageView.setOnTouchListener(mTouchListener);

        // set up of buttons for image - in Edit mode and in Insert mode
        //for Edit mode
        ImageView editPictureButton = (ImageView) findViewById(R.id.button_edit_picture);
        editPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

        //for Insert mode
        ImageView uploadPictureButton = (ImageView) findViewById(R.id.button_upload_picture);
        uploadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

        //Set proper visibility for image button - different for edit and upload mode
        if (mCurrentPolaroidUri == null) {
            editPictureButton.setVisibility(View.GONE);
        } else {
            uploadPictureButton.setVisibility(View.GONE);
        }
    }

    // Gets user input from the editor and saves new polaroid into a database
    private void savePolaroid() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        // return if there is no data input while inserting a product
        if (mCurrentPolaroidUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(supplierString)
                && mUri == null) {
            finish();
        }

        // gets user inut from image upload
        String imageUriString = "";
        if (mUri != null) {
            imageUriString = mUri.toString();
        } else {
            Log.v(LOG_TAG, getString(R.string.editor_image_not_inserted));
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PolaroidEntry.COLUMN_POLAROID_NAME, nameString);
        values.put(PolaroidEntry.COLUMN_POLAROID_QTY, quantityString);
        values.put(PolaroidEntry.COLUMN_POLAROID_PRICE, priceString);
        values.put(PolaroidEntry.COLUMN_POLAROID_SUPPLIER, supplierString);
        values.put(PolaroidEntry.COLUMN_POLAROID_PICTURE, imageUriString);


        // saving new product
        if (mCurrentPolaroidUri == null) {

        //checks that required field are input : name, price, quantity, picture
            if(nameString.isEmpty() || quantityString.isEmpty() ||
                    priceString.isEmpty() || imageUriString.isEmpty()) {
                Toast.makeText(this, getString(R.string.editor_required_fields), Toast.LENGTH_LONG).show();

            }
            else {
                // Insert the new row, returning the primary key value of the new row
                Uri newUri = getContentResolver().insert(PolaroidEntry.CONTENT_URI, values);

                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.editor_insert_product_failed), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_product_successful), Toast.LENGTH_LONG).show();
                    finish();
                }
            }


        } else {
            // Otherwise this is an EXISTING product, so update the polaropid with content URI: mCurrentPolaroidUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPolaroidUri will already identify the correct row in the database that
            // we want to modify.
            if(mNameEditText.length() == 0 || mQuantityEditText.length() == 0 ||
                    mPriceEditText.length() == 0 || Uri.EMPTY.equals(mUri)) {

                Toast.makeText(this, getString(R.string.editor_required_fields), Toast.LENGTH_LONG).show();

            }
            else{
                int rowsAffected = getContentResolver().update(mCurrentPolaroidUri, values, null, null);
                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentPolaroidUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save polaroid to db
                savePolaroid();


                // request at least name to be inserted
              //  String nameString = mNameEditText.getText().toString().trim();
              //  String quantityString = mQuantityEditText.getText().toString().trim();
               // String priceString = mPriceEditText.getText().toString().trim();

                //String imageUriString = mUri.toString();

              //  if (nameString.isEmpty() || quantityString.isEmpty() || priceString.isEmpty()) {
               //     Toast.makeText(this, "nahrej nazev, quantitu, cenu, nebo zemri", Toast.LENGTH_LONG).show();;


              //  } else {
                    //Exit activity
                 //   finish();
              //  }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the polaroid hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mPolaroidHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mPolaroidHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.

        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                PolaroidEntry._ID,
                PolaroidEntry.COLUMN_POLAROID_NAME,
                PolaroidEntry.COLUMN_POLAROID_QTY,
                PolaroidEntry.COLUMN_POLAROID_PRICE,
                PolaroidEntry.COLUMN_POLAROID_SUPPLIER,
                PolaroidEntry.COLUMN_POLAROID_PICTURE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentPolaroidUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
// Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(PolaroidEntry.COLUMN_POLAROID_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(PolaroidEntry.COLUMN_POLAROID_QTY);
            int priceColumnIndex = cursor.getColumnIndex(PolaroidEntry.COLUMN_POLAROID_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(PolaroidEntry.COLUMN_POLAROID_SUPPLIER);
            int pictureColumnIndex = cursor.getColumnIndex(PolaroidEntry.COLUMN_POLAROID_PICTURE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);

            // when picture is not uploaded
            String pictureString = cursor.getString(pictureColumnIndex);
            if (pictureString != null) {
                Uri imageUri = Uri.parse(pictureString);
                Bitmap imageBitmap = getBitmapFromUri(imageUri);
                mProductImageView.setImageBitmap(imageBitmap);
            } else {
                Log.v(LOG_TAG, getString(R.string.editor_image_not_inserted));
                            }

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            mSupplierEditText.setText(supplier);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
// If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
        mProductImageView.setImageBitmap(null);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deletePolaroid();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deletePolaroid() {
        // Only perform the delete if this is an existing product.
        if (mCurrentPolaroidUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPolaroidUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPolaroidUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_LONG).show();
            }
            // Close the activity
            finish();
        }
    }

    // method for + button - increase of quantity
    public void increaseQuantity(View view) {
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity;
        if (quantityString.matches("")){
            quantity=0;
        }
        else{
            quantity = Integer.parseInt(quantityString);
        }
        quantity = quantity + 1;
        mQuantityEditText.setText(String.valueOf(quantity));
    }

    // method for - button - decrease of quantity
    public void decreaseQuantity(View view) {
       String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity;
    if(quantityString.matches("")){
        quantity = 0;
    }
     else{
        quantity = Integer.parseInt(quantityString);}
            quantity = quantity - 1;
        if (quantity < 0) {
            quantity = 0;
            Toast.makeText(this, getString(R.string.editor_no_available_products), Toast.LENGTH_LONG).show();
        }
        mQuantityEditText.setText(String.valueOf(quantity));
    }

    // method for Order button - intent with sending an order email
    public void sendOrderEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        String supplierEmailAddress = mSupplierEditText.getText().toString().trim();
        intent.setData(Uri.parse("mailto:" + supplierEmailAddress));
        String productName = mNameEditText.getText().toString().trim();
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().
                getString(R.string.mail_subject) + " " + productName);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    //opens gallery of pictures in the device
    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri of picture: " + mUri.toString());
                mProductImageView.setImageBitmap(getBitmapFromUri(mUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mProductImageView.getWidth();
        int targetH = mProductImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
Log.v(LOG_TAG, "rozmery vyska:" + photoH +"sirka:" + photoW);
            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {
            }
        }
    }
}