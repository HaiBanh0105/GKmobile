package com.example.gk;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ExchangeRateActivity extends AppCompatActivity {
    private ExchangeDAO dao;
    private RecyclerView recyclerView;
    private ExchangeRateAdapter adapter;
    private List<ExchangeRate> rateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);

        dao = AppDatabase.getInstance(getApplicationContext()).exchangeDAO();
        recyclerView = findViewById(R.id.recyclerRates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExchangeRateAdapter(this, rateList, dao);
        recyclerView.setAdapter(adapter);

        Button btnAdd = findViewById(R.id.btnAddRate);
        btnAdd.setOnClickListener(v -> showAddDialog());

        loadRates();
    }

    private void loadRates() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ExchangeRate> rates = dao.getListExchangeRates();
            runOnUiThread(() -> {
                rateList.clear();
                rateList.addAll(rates);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exchange_rate, null);

        EditText edtCode = dialogView.findViewById(R.id.edtCurrencyCode);
        EditText edtRate = dialogView.findViewById(R.id.edtExchangeRate);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Thêm tỷ giá mới")
                .setPositiveButton("Thêm", null) // xử lý sau để kiểm tra dữ liệu
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                String code = edtCode.getText().toString().trim();
                String rateStr = edtRate.getText().toString().trim();

                if (code.isEmpty() || rateStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double rate = Double.parseDouble(rateStr);
                    ExchangeRate newRate = new ExchangeRate(code, rate, System.currentTimeMillis());

                    Executors.newSingleThreadExecutor().execute(() -> {
                        dao.insert(newRate);
                        runOnUiThread(() -> {
                            loadRates();
                            dialog.dismiss();
                        });
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Tỷ giá không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }


}
