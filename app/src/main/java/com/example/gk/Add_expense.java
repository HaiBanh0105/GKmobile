package com.example.gk;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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
        List<String> categories = Arrays.asList("Ăn uống", "Giải trí", "Lương", "Hóa đơn");
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
            List<String> currencyList = AppDatabase.getInstance(getApplicationContext())
                    .exchangeDAO()
                    .getAllCurrencyCodes();

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, currencyList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.currency.setAdapter(adapter);

                binding.currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selected = parent.getItemAtPosition(position).toString();
                        viewModel.setCurrency(selected);
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
            viewModel.setTimestamp(System.currentTimeMillis());
            viewModel.saveExpense(this);
            Toast.makeText(this, "Đã lưu giao dịch", Toast.LENGTH_SHORT).show();
        });

        binding.btnCancel.setOnClickListener(v -> finish());

//        binding.btnManageCategory.setOnClickListener(v -> {
//            CategoryDialogFragment dialog = new CategoryDialogFragment();
//            dialog.show(getSupportFragmentManager(), "CategoryDialog");
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCurrencySpinner(); // reload khi quay lại
    }
}
