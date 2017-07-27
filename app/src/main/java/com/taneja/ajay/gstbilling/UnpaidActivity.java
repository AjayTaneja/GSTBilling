package com.taneja.ajay.gstbilling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.taneja.ajay.gstbilling.data.GSTBillingContract;

public class UnpaidActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, BillAdapter.BillItemClickListener {

    private RecyclerView unpaidRecyclerView;
    private BillAdapter adapter;

    private static final int BILL_LOADER_ID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unpaid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.unpaid_bills_activity_title);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_unpaid);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UnpaidActivity.this, NewBillCustomerActivity.class));
            }
        });

        checkPasswordSetup();

        unpaidRecyclerView = (RecyclerView) findViewById(R.id.unpaid_recycler_view);
        unpaidRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        unpaidRecyclerView.setHasFixedSize(true);
        adapter = new BillAdapter(this, this, Color.RED);
        unpaidRecyclerView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(BILL_LOADER_ID, null, this);

    }

    private void checkPasswordSetup() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString(SetupPasswordActivity.SETUP_PASSWORD_KEY, null) == null){
            Intent intent = new Intent(this, SetupPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_unpaid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_show_paid_bills){
            Intent intent = new Intent(this, PaidActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case BILL_LOADER_ID:
                return new CursorLoader(
                        this,
                        GSTBillingContract.GSTBillingEntry.CONTENT_URI,
                        null,
                        GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS + "='" + GSTBillingContract.BILL_STATUS_UNPAID + "'",
                        null,
                        GSTBillingContract.GSTBillingEntry._ID
                );
            default:
                throw new RuntimeException("Loader not implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onBillItemClick(String  clickedBillId, String customerName, String phoneNumber) {
        Intent detailIntent = new Intent(this, DetailActivity.class);

        detailIntent.putExtra(GSTBillingContract.GSTBillingEntry._ID, clickedBillId);
        detailIntent.putExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_NAME, customerName);
        detailIntent.putExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_PHONE_NUMBER, phoneNumber);

        startActivity(detailIntent);
    }
}
