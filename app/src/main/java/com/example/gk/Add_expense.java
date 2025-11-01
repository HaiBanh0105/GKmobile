package com.example.gk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.gk.Database.AppDatabase;
import com.example.gk.viewmodel.ExpenseViewModel;
import com.example.gk.databinding.AddExpenseBinding;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class Add_expense extends BaseActivity {

    private AddExpenseBinding binding;
    private ExpenseViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar(R.id.toolbarDashboard);

        viewModel = new ExpenseViewModel(new Expense());
        binding.setExpenseViewModel(viewModel);
        binding.setLifecycleOwner(this);

        setupCategorySpinner();
        setupCurrencySpinner();
        setupRadioGroup();
        setupButtons();
    }

    private void setupCategorySpinner() {
        List<String> categories = Arrays.asList(
                "Lương",
                "Ăn uống",
                "Giải trí",
                "Hóa đơn",
                "Di chuyển",
                "Mua sắm",
                "Sức khỏe",
                "Giáo dục",
                "Du lịch",
                "Gia đình",
                "Tiết kiệm",
                "Đầu tư",
                "Quà tặng",
                "Thuế",
                "Vay nợ",
                "Khác"
        );
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        binding.category.setAdapter(adapter);

        binding.category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                viewModel.setCategory(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupCurrencySpinner() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CurrencyInfo> currencyList = AppDatabase.getInstance(getApplicationContext())
                    .currencyDAO()
                    .getAllCurrencies();

            runOnUiThread(() -> {
                ArrayAdapter<CurrencyInfo> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, currencyList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.currency.setAdapter(adapter);

                binding.currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        CurrencyInfo selected = (CurrencyInfo) parent.getItemAtPosition(position);
                        viewModel.setCurrency(selected.code); // chỉ lấy mã tiền tệ để xử lý
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        });
    }


    private void setupRadioGroup() {
        binding.radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isIncome = (checkedId == R.id.radioIncome);
            viewModel.setIncome(isIncome);
        });
    }

    private void setupButtons() {
        binding.btnAdd.setOnClickListener(v -> {
            if (!viewModel.isValid()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            Executors.newSingleThreadExecutor().execute(() -> {
                viewModel.setTimestamp(System.currentTimeMillis());
                viewModel.saveExpense(this);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã lưu giao dịch", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, Dashboard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            });
        });

        binding.btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc muốn hủy?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        Intent intent = new Intent(this, Dashboard.class);
                        setResult(RESULT_CANCELED); // hoặc truyền thêm dữ liệu qua intent
                        finish();
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCurrencySpinner(); // reload khi quay lại
    }
}
