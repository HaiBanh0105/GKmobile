package com.example.gk;

import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class BaseActivity extends AppCompatActivity {
    protected void setupToolbar(int toolbarId) {
        MaterialToolbar toolbar = findViewById(toolbarId);
        toolbar.setTitle("Expense Tracker Pro");
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_currency) {
                showCurrencyDialog();
                return true;}
            return false;
        });
    }

    public void showCurrencyDialog() {
//        String[] currencies = {"VND", "USD", "EUR", "JPY"};
        ExchangeDAO dao = AppDatabase.getInstance(this).exchangeDAO();
        List<String> currencyList = dao.getAllBaseCurrencies();
        // Thêm "VND" nếu chưa có
        if (!currencyList.contains("VND")) {
            currencyList.add("VND");
        }
        String[] currencies = currencyList.toArray(new String[0]);


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
