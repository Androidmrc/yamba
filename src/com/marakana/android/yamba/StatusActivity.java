package com.marakana.android.yamba;

import android.os.Bundle;
import android.view.MenuItem;


public class StatusActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
   }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (item.getItemId() == R.id.item_status)
            ? true
            : super.onOptionsItemSelected(item);
    }
}
