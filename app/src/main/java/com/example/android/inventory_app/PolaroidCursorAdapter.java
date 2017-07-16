package com.example.android.inventory_app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory_app.data.PolaroidContract;
import com.example.android.inventory_app.data.PolaroidContract.PolaroidEntry;
import com.example.android.inventory_app.data.PolaroidDbHelper;

import static android.R.attr.id;
import static android.R.attr.name;
import static com.example.android.inventory_app.R.id.fab;
import static java.security.AccessController.getContext;

/**
 * Created by evi on 15. 7. 2017.
 */

public class PolaroidCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = PolaroidCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link PolaroidCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public PolaroidCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the polaroid data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current polaroid can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        final TextView tvQuantity = (TextView) view.findViewById(R.id.tvQuantity);
        TextView tvPrice = (TextView) view.findViewById(R.id.tvPrice);
        Button sellButton = (Button) view.findViewById(R.id.sell_button);

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(PolaroidEntry.COLUMN_POLAROID_NAME));
        final int quantityInt = cursor.getInt(cursor.getColumnIndexOrThrow(PolaroidEntry.COLUMN_POLAROID_QTY));
        String quantity = String.valueOf(quantityInt);
        int priceInt = cursor.getInt(cursor.getColumnIndexOrThrow(PolaroidEntry.COLUMN_POLAROID_PRICE));
        String price = String.valueOf(priceInt);
        // get position for the onclick below
        final int position = cursor.getPosition();

        // set up onClickListener for the Sell button
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get curent cursor to be able to get id
                cursor.moveToPosition(position);
                //get id of the product to be able to construct uri
                long id = cursor.getLong(cursor.getColumnIndex(PolaroidEntry._ID));
                //uri construction
                Uri uri = ContentUris.withAppendedId(PolaroidEntry.CONTENT_URI, id);

                //update ContentValues
                ContentValues values = new ContentValues();
                int quantity = Integer.parseInt(tvQuantity.getText().toString());
                quantity = quantity - 1;
                if (quantity < 0) {
                    quantity = 0;
                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.editor_no_available_products), Toast.LENGTH_LONG).show();
                }
                values.put(PolaroidEntry.COLUMN_POLAROID_QTY, quantity);
                int rowsAffected = view.getContext().getContentResolver().update(uri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Log.v(LOG_TAG, view.getContext().getString(R.string.adapter_update_product_failed));
                } else {
                    // Otherwise, the update was successful and we can log it.
                    Log.v(LOG_TAG, view.getContext().getString(R.string.adapter_update_product_successful));
                }
            }
        });


        // If the product price is empty string or null, then use some default text
        // that says "Price Unknown", so the TextView isn't blank.
        if (TextUtils.isEmpty(price)) {
            price = context.getString(R.string.unknown_price);
        }

        // Populate fields with extracted properties
        tvName.setText(name);
        tvQuantity.setText(quantity);
        tvPrice.setText(price);

    }
}
