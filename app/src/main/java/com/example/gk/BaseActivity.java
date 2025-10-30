package com.example.gk;

import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
            else if (id == R.id.menu_currency) {
                showCurrencyDialog();
                return true;}
            return false;
        });
    }

    private void showCurrencyDialog() {
        String[] currencies = {"VND", "USD", "EUR", "JPY"};

        // Tạo layout Spinner
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, currencies);
        spinner.setAdapter(adapter);

        // Đặt giá trị hiện tại nếu có
        int currentIndex = java.util.Arrays.asList(currencies).indexOf(AppConstants.currentCurrency);
        if (currentIndex >= 0) spinner.setSelection(currentIndex);

        // Tạo Dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chọn đơn vị tiền tệ")
                .setView(spinner)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String selected = spinner.getSelectedItem().toString();
                    AppConstants.currentCurrency = selected;

                    // Nếu đang ở Dashboard thì gọi lại loadSummary()
                    if (BaseActivity.this instanceof Dashboard) {
                        ((Dashboard) BaseActivity.this).loadSummary();
                    }
                    if (BaseActivity.this instanceof Statistics) {
                        ((Statistics) BaseActivity.this).filterAndDisplay();;
                    }
                    // Có thể gọi hàm cập nhật giao diện nếu cần
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


}
