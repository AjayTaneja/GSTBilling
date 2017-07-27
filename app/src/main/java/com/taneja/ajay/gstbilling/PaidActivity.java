package com.taneja.ajay.gstbilling;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.taneja.ajay.gstbilling.data.GSTBillingContract;

public class PaidActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, BillAdapter.BillItemClickListener {

    private static final int BILL_PAID_LOADER_ID = 200;

    private RecyclerView paidRecyclerView;
    private BillAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PaidActivity.this, NewBillCustomerActivity.class));
            }
        });

        paidRecyclerView = (RecyclerView) findViewById(R.id.paid_recycler_view);
        paidRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paidRecyclerView.setHasFixedSize(true);
        adapter = new BillAdapter(this, this, Color.GREEN);
        paidRecyclerView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(BILL_PAID_LOADER_ID, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_paid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_show_unpaid_bills){
            Intent intent = new Intent(this, UnpaidActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case BILL_PAID_LOADER_ID:
                return new CursorLoader(
                        this,
                        GSTBillingContract.GSTBillingEntry.CONTENT_URI,
                        null,
                        GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS + "='" + GSTBillingContract.BILL_STATUS_PAID + "'",
                        null,
                        GSTBillingContract.GSTBillingEntry._ID + " DESC"
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
        detailIntent.putExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_STATUS, GSTBillingContract.BILL_STATUS_PAID);
        detailIntent.putExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_NAME, customerName);
        detailIntent.putExtra(GSTBillingContract.GSTBillingEntry.PRIMARY_COLUMN_PHONE_NUMBER, phoneNumber);

        startActivity(detailIntent);
    }
}
