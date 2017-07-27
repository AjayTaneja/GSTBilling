package com.taneja.ajay.gstbilling;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.taneja.ajay.gstbilling.data.GSTBillingContract;
import com.taneja.ajay.gstbilling.utils.PriceUtils;

/**
 * Created by Ajay on 7/24/2017.
 */

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.DetailHolder> {

    private Cursor mCursor;
    private Context mContext;

    float totalTaxableValue = 0;
    float totalSingleGst = 0;
    float totalAmount = 0;


    public DetailAdapter(Cursor mCursor, Context mContext) {
        this.mCursor = mCursor;
        this.mContext = mContext;
    }

    @Override
    public DetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_item_detail_layout, parent, false);
        return new DetailHolder(view);
    }

    @Override
    public void onBindViewHolder(DetailHolder holder, int position) {
        if(mCursor.moveToPosition(position)){

            int idValue = mCursor.getInt(mCursor.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry._ID));
            String itemDescriptionValue = mCursor.getString(mCursor.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_ITEM_DESCRIPTION));
            float finalPriceValue = mCursor.getFloat(mCursor.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_FINAL_PRICE));
            int quantityValue = mCursor.getInt(mCursor.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_QUANTITY));
            int taxSlabValue = mCursor.getInt(mCursor.getColumnIndex(GSTBillingContract.GSTBillingCustomerEntry.SECONDARY_COLUMN_TAX_SLAB));

            PriceUtils priceUtils = new PriceUtils(finalPriceValue, quantityValue, taxSlabValue);
            float rateValue = priceUtils.getRate();
            float taxableValueValue = priceUtils.getTaxableValue();
            float singleGstValue = priceUtils.getSingleGst();

            holder.itemDescription.setText(itemDescriptionValue);
            holder.sno.setText(String.valueOf(idValue));
            holder.finalPrice.setText(String.format("%.2f", finalPriceValue));
            holder.qty.setText(String.valueOf(quantityValue));
            holder.rate.setText(String.format("%.2f", rateValue));
            holder.taxableValue.setText(String.format("%.2f", taxableValueValue));
            holder.taxSlab.setText(String.valueOf(taxSlabValue) + "%");
            holder.cgst.setText(String.format("%.2f", singleGstValue));
            holder.sgst.setText(String.format("%.2f", singleGstValue));

            totalTaxableValue += taxableValueValue;
            totalSingleGst += singleGstValue;
            totalAmount += (finalPriceValue*quantityValue);

            if (position == getItemCount()-1){
                DetailActivity.printTotalDetails(totalTaxableValue, totalSingleGst, totalAmount);
            }

        }
    }

    @Override
    public int getItemCount() {
        if(mCursor == null){
            return 0;
        }else {
            return mCursor.getCount();
        }
    }

    public class DetailHolder extends RecyclerView.ViewHolder {

        TextView itemDescription;
        TextView sno, finalPrice, qty, rate, taxableValue, taxSlab, cgst, sgst;

        public DetailHolder(View itemView) {
            super(itemView);

            itemDescription = (TextView) itemView.findViewById(R.id.detail_item);
            sno = (TextView) itemView.findViewById(R.id.detail_sno);
            finalPrice = (TextView) itemView.findViewById(R.id.detail_final_price);
            qty = (TextView) itemView.findViewById(R.id.detail_quantity);
            rate = (TextView) itemView.findViewById(R.id.detail_rate);
            taxableValue = (TextView) itemView.findViewById(R.id.detail_taxable_value);
            taxSlab = (TextView) itemView.findViewById(R.id.detail_tax_slab);
            cgst = (TextView) itemView.findViewById(R.id.detail_cgst);
            sgst = (TextView) itemView.findViewById(R.id.detail_sgst);
        }
    }
}
