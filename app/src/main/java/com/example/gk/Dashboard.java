package com.example.gk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Dashboard extends AppCompatActivity {

    FloatingActionButton AddExpense;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dashboard);

        AddExpense = findViewById(R.id.btnAddExpense);

        AddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, add_expense.class);
                startActivity(intent);
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbarDashboard);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_statistics) {
                startActivity(new Intent(Dashboard.this, Statistics.class));
                return true;}
//            else if (id == R.id.menu_search) {
//                startActivity(new Intent(Dashboard.this, SearchActivity.class));
//                return true;
//            } else if (id == R.id.menu_settings) {
//                startActivity(new Intent(Dashboard.this, SettingsActivity.class));
//                return true;
//            }
            return false;
        });

    }
}