package com.taneja.ajay.gstbilling;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.taneja.ajay.gstbilling.data.GSTBillingContract;
import com.taneja.ajay.gstbilling.utils.NumberToWord;
import com.taneja.ajay.gstbilling.utils.PDFUtils;

import java.util.ArrayList;
import java.util.List;

public class SavePDFActivity extends AppCompatActivity {

    private static final int MAX_ITEMS_PER_PAGE = 32;
    private Cursor pdfCursor;
    private int totalItems;
    private int itemsShown;
    private PDFUtils pdfMaker;
    private boolean moreItemsPresent;

    private int totalPages;
    private int pageNumber;

    private static String customerName;
    private static String billId;

    private TextView customerNameTv;
    private TextView invoiceDateTv;
    private static TextView totalQtyTv;
    private static TextView totalTaxableValueTv;
    private static TextView totalCgstTv;
    private static TextView totalSgstTv;
    private static TextView totalAmountBeforeTaxTv;
    private static TextView totalTaxAmountTv;
    private static TextView totalAmountAfterTaxTv;
    private static TextView totalAmountInWordsTv;
    private static TextView billPageNumber;

    private RecyclerView pdfRecyclerView;
    private SavePDFAdapter adapter;

    private static ContentResolver contentResolver;

    private Intent getPDFIntent;

    private static String inr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_pdf);

        getPDFIntent = getIntent();
        customerName = getPDFIntent.getStringExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_NAME);
        billId = getPDFIntent.getStringExtra(GSTBillingContract.GSTBillingEntry._ID);
        totalItems = getPDFIntent.getIntExtra(DetailActivity.ITEM_COUNT_KEY, 0);

        totalPages = (int) Math.ceil((double)totalItems/MAX_ITEMS_PER_PAGE);
        pageNumber = 0;

        customerNameTv = (TextView) findViewById(R.id.pdf_customer_name);
        invoiceDateTv = (TextView) findViewById(R.id.pdf_invoice_date);
        totalQtyTv = (TextView) findViewById(R.id.pdf_total_qty);
        totalTaxableValueTv = (TextView) findViewById(R.id.pdf_total_taxable_value);
        totalCgstTv = (TextView) findViewById(R.id.pdf_total_cgst);
        totalSgstTv = (TextView) findViewById(R.id.pdf_total_sgst);
        totalAmountBeforeTaxTv = (TextView) findViewById(R.id.pdf_total_amount_before_tax);
        totalTaxAmountTv = (TextView) findViewById(R.id.pdf_total_gst);
        totalAmountAfterTaxTv = (TextView) findViewById(R.id.pdf_total_amount_after_tax);
        totalAmountInWordsTv = (TextView) findViewById(R.id.pdf_total_amount_in_words);
        billPageNumber = (TextView) findViewById(R.id.pdf_bill_page_number);

        customerNameTv.setText(customerName);
        Cursor billDateCursor = getContentResolver().query(
                GSTBillingContract.GSTBillingEntry.CONTENT_URI,
                new String[]{GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_DATE},
                GSTBillingContract.GSTBillingEntry._ID + "=" + billId,
                null,
                null
        );
        billDateCursor.moveToFirst();
        String billDate = billDateCursor.getString(0);
        invoiceDateTv.setText(billDate);

        inr = getString(R.string.inr) + " ";

        contentResolver = getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pdfMaker = new PDFUtils();
        }

        itemsShown = 0;
        pdfCursor = updatePdfCursor();

        pdfRecyclerView = (RecyclerView) findViewById(R.id.pdf_recycler_view);
        pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pdfRecyclerView.setHasFixedSize(true);
        adapter = new SavePDFAdapter(this, pdfCursor);
        pdfRecyclerView.setAdapter(adapter);

    }

    private Cursor updatePdfCursor() {
        Cursor cursor = contentResolver.query(
                GSTBillingContract.GSTBillingEntry.CONTENT_URI.buildUpon().appendPath(billId).build(),
                null,
                GSTBillingContract.GSTBillingCustomerEntry._ID + " between " + (itemsShown + 1) + " and " + (itemsShown + MAX_ITEMS_PER_PAGE),
                null,
                GSTBillingContract.GSTBillingCustomerEntry._ID
        );
        itemsShown += MAX_ITEMS_PER_PAGE;
        moreItemsPresent = areMoreItemsPresent();
        setBillPageNumber();
        return cursor;
    }

    public boolean areMoreItemsPresent(){
        if((totalItems - itemsShown) > 0){
            return true;
        }else {
            return false;
        }
    }

    private void setBillPageNumber(){
        pageNumber++;
        billPageNumber.setText("Page " + pageNumber + " of " + totalPages);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save_pdf, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_save_pdf_file:
                pdfMaker.addPageToPDF(findViewById(R.id.pdf_view));
                if(moreItemsPresent){
                    pdfCursor = updatePdfCursor();
                    adapter.swapCursor(pdfCursor);
                }else {
                    pdfMaker.createPdf(this, billId + " " + customerName);
                    finish();
                }
        }

        return super.onOptionsItemSelected(item);
    }

    public static void printTotalDetails(int totalQty, float totalTaxableValue, float totalSingleGst, float totalAmount){
        totalQtyTv.setText(String.valueOf(totalQty));
        totalTaxableValueTv.setText(String.format("%.2f", totalTaxableValue));
        totalCgstTv.setText(String.format("%.2f", totalSingleGst));
        totalSgstTv.setText(String.format("%.2f", totalSingleGst));
        totalAmountBeforeTaxTv.setText(inr + String.format("%.2f", totalTaxableValue));
        totalTaxAmountTv.setText(inr + String.format("%.2f", (totalSingleGst+totalSingleGst)));
        totalAmountAfterTaxTv.setText(inr + String.format("%.2f", totalAmount));
        totalAmountInWordsTv.setText("Rupees. " + NumberToWord.getNumberInWords(String.valueOf((int)totalAmount)));
    }
}
