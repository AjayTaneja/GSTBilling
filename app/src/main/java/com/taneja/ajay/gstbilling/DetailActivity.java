package com.taneja.ajay.gstbilling;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.taneja.ajay.gstbilling.data.GSTBillingContract;
import com.taneja.ajay.gstbilling.utils.NumberToWord;

public class DetailActivity extends AppCompatActivity {

    private static final int ACTION_MARK_AS_PAID_ID = 400;
    private static final int ACTION_DELETE_BILL_ID = 401;

    private RecyclerView detailRecyclerView;
    private DetailAdapter adapter;
    private Cursor detailCursor;
    private String billId;
    private String billStatus;
    private String phoneNumber;

    private static TextView totalTaxableValueTv;
    private static TextView totalCgstTv;
    private static TextView totalSgstTv;
    private static TextView totalGstTv;
    private static TextView totalAmountTv;
    private static TextView totalAmountInWordsTv;

    private static String inr;

    private ActionBar detailActionBar;

    private Intent getDetailIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailActionBar = getSupportActionBar();

        getDetailIntent = getIntent();
        billId = getDetailIntent.getStringExtra(GSTBillingContract.GSTBillingEntry._ID);

        if(getDetailIntent.hasExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS)){
            billStatus = getDetailIntent.getStringExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS);
            detailActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00C853")));
        }else {
            billStatus = GSTBillingContract.BILL_STATUS_UNPAID;
            detailActionBar.setBackgroundDrawable(new ColorDrawable(Color.RED));
        }

        if(getDetailIntent.hasExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_NAME)){
            String customerName = getDetailIntent.getStringExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_NAME);
            detailActionBar.setTitle(customerName);
        }

        if(getDetailIntent.hasExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_PHONE_NUMBER)){
            phoneNumber = getDetailIntent.getStringExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_PHONE_NUMBER);
        }

        detailCursor = getContentResolver().query(GSTBillingContract.GSTBillingEntry.CONTENT_URI.buildUpon().appendPath(billId).build(), null, null, null, null);

        detailRecyclerView = (RecyclerView) findViewById(R.id.detail_recycler_view);
        detailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        detailRecyclerView.setHasFixedSize(true);
        adapter = new DetailAdapter(detailCursor, this);
        detailRecyclerView.setAdapter(adapter);

        totalTaxableValueTv = (TextView) findViewById(R.id.total_amount_before_tax_value);
        totalCgstTv = (TextView) findViewById(R.id.total_cgst_value);
        totalSgstTv = (TextView) findViewById(R.id.total_sgst_value);
        totalGstTv = (TextView) findViewById(R.id.total_gst_value);
        totalAmountTv = (TextView) findViewById(R.id.total_amount_after_tax_value);
        totalAmountInWordsTv = (TextView) findViewById(R.id.total_amount_in_words_value);

        inr = getString(R.string.inr) + " ";

    }

    public static void printTotalDetails(float totalTaxableValue, float totalSingleGst, float totalAmount){
        totalTaxableValueTv.setText(inr + String.format("%.2f", totalTaxableValue));
        totalCgstTv.setText(inr + String.format("%.2f", totalSingleGst));
        totalSgstTv.setText(inr + String.format("%.2f", totalSingleGst));
        totalGstTv.setText(inr + String.format("%.2f", (totalSingleGst+totalSingleGst)));
        totalAmountTv.setText(inr + String.format("%.2f", totalAmount));
        totalAmountInWordsTv.setText("Rupees. " + NumberToWord.getNumberInWords(String.valueOf((int)totalAmount)));
    }

    private void markAsPaid(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS, GSTBillingContract.BILL_STATUS_PAID);
        getContentResolver().update(
                GSTBillingContract.GSTBillingEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(billId)).build(),
                contentValues,
                GSTBillingContract.GSTBillingEntry._ID + "=" + billId,
                null
        );
        Toast.makeText(this, getString(R.string.mark_as_paid_success), Toast.LENGTH_LONG).show();

        billStatus = GSTBillingContract.BILL_STATUS_PAID;
        detailActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00C853")));
        getDetailIntent.putExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS, billStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_mark_as_paid){
            if(billStatus == GSTBillingContract.BILL_STATUS_UNPAID){

                displayPasswordDialog(ACTION_MARK_AS_PAID_ID);

            }else {
                Toast.makeText(this, getString(R.string.marked_as_paid_already), Toast.LENGTH_LONG).show();
            }
        }else if(id == R.id.action_call_customer){
            if(phoneNumber != null && phoneNumber.length() == 10){
                Intent callIntent = new Intent(Intent.ACTION_VIEW);
                callIntent.setData(Uri.parse("tel:" + "+91" + phoneNumber));
                startActivity(callIntent);
            }else {
                Toast.makeText(this, getString(R.string.no_phone_number_error), Toast.LENGTH_SHORT).show();
            }
        }else if(id == R.id.action_delete_bill){
            if(billId != null && billId.length() != 0){

                displayPasswordDialog(ACTION_DELETE_BILL_ID);

            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteBill() {
        int rowsDeleted = getContentResolver().delete(GSTBillingContract.GSTBillingEntry.CONTENT_URI.buildUpon().appendPath(billId).build(), null, null);
        if(rowsDeleted == 1){
            Toast.makeText(this, getString(R.string.delete_bill_success), Toast.LENGTH_SHORT).show();
            finish();
        }else {
            Toast.makeText(this, getString(R.string.delete_bill_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPasswordDialog(final int actionId) {

        String title = getString(R.string.action_mark_as_paid_label);
        if(actionId == ACTION_DELETE_BILL_ID){
            title = getString(R.string.action_delete_bill_label);
        }

        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordInput.setHint(R.string.enter_password_dialog_hint);
        passwordInput.setHintTextColor(Color.LTGRAY);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(passwordInput)
                .setPositiveButton(getString(R.string.enter_password_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = passwordInput.getText().toString();
                        String savedPassword = PreferenceManager.getDefaultSharedPreferences(DetailActivity.this)
                                .getString(SetupPasswordActivity.SETUP_PASSWORD_KEY, null);
                        if(savedPassword != null && savedPassword.equals(password)){

                            switch (actionId){
                                case ACTION_MARK_AS_PAID_ID:
                                    markAsPaid();
                                    break;
                                case ACTION_DELETE_BILL_ID:
                                    deleteBill();
                                    break;
                                default:
                                    Toast.makeText(DetailActivity.this, getString(R.string.no_operation_specified_error), Toast.LENGTH_SHORT).show();
                            }

                        }else {
                            Toast.makeText(DetailActivity.this, getString(R.string.invalid_password_error), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.enter_password_dialog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();

    }
}
