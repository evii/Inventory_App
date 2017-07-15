package com.example.android.inventory_app;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory_app.data.PolaroidContract;

/**
 * Created by evi on 15. 7. 2017.
 */

public class PolaroidCursorAdapter extends CursorAdapter {

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
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        TextView tvQuantity = (TextView) view.findViewById(R.id.tvQuantity);
        TextView tvPrice = (TextView) view.findViewById(R.id.tvPrice);

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(PolaroidContract.PolaroidEntry.COLUMN_POLAROID_NAME));
        int quantityInt = cursor.getInt(cursor.getColumnIndexOrThrow(PolaroidContract.PolaroidEntry.COLUMN_POLAROID_QTY));
        String quantity = String.valueOf(quantityInt);
        int priceInt = cursor.getInt(cursor.getColumnIndexOrThrow(PolaroidContract.PolaroidEntry.COLUMN_POLAROID_PRICE));
        String price = String.valueOf(priceInt);


        // If the pet breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.
        if (TextUtils.isEmpty(price)) {
            price = context.getString(R.string.unknown_price);
        }

        // Populate fields with extracted properties
        tvName.setText(name);
        tvQuantity.setText(quantity);
        tvPrice.setText(price);

    }
}
