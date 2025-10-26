package com.example.gk;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class BaseActivity extends AppCompatActivity {
    protected void setupToolbar(int toolbarId) {
        MaterialToolbar toolbar = findViewById(toolbarId);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_statistics) {
                startActivity(new Intent(this, Statistics.class));
                return true;}
            else if (id == R.id.menu_dashboard) {
                startActivity(new Intent(this, Dashboard.class));
                return true;}
            return false;
        });
    }
}
