package com.example.gk;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gk.viewmodel.StatisticsViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Statistics extends BaseActivity {

    private StatisticsViewModel viewModel;
    private ExpenseAdapter expenseAdapter;

    private RecyclerView rcv;
    private Spinner spinnerMonth;
    private EditText edtYear, edtSearch;
    private Button btnExport;

    private ImageButton btnBack;

    private String incomeStr, expenseStr, differenceStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);
        setupToolbar(R.id.toolbarDashboard);

        viewModel = new StatisticsViewModel();
        viewModel.loadAllExpenses(this);

        initUi();
        setupRecyclerView();
        setupMonthSpinner();
        setupListeners();
        filterAndDisplay();
    }

    private void initUi() {
        rcv = findViewById(R.id.rcv_expense);
        spinnerMonth = findViewById(R.id.spnMonthFilter);
        edtYear = findViewById(R.id.edtYear);
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        btnExport = findViewById(R.id.btnExportPdf);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        edtYear.setText(String.valueOf(currentYear));
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter();
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(expenseAdapter);
    }

    private void setupMonthSpinner() {
        String[] months = {"Không chọn", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        spinnerMonth.setSelection(currentMonth);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplay();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplay();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        edtYear.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplay();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        btnExport.setOnClickListener(v -> {
            String yearText = edtYear.getText().toString().trim();
            int monthText = spinnerMonth.getSelectedItemPosition();

            if (yearText.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập năm trước khi xuất báo cáo", Toast.LENGTH_SHORT).show();
                return;
            }
            if (monthText == 0) {
                Toast.makeText(this, "Vui lòng chọn tháng trước khi xuất báo cáo", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, Export_report.class);
            intent.putExtra("month", monthText);
            intent.putExtra("year", yearText);
            intent.putExtra("totalIncome", incomeStr);
            intent.putExtra("totalExpense", expenseStr);
            intent.putExtra("Difference", differenceStr);
            intent.putExtra("ListExpense", new ArrayList<>(viewModel.filteredExpenses));

            startActivity(intent);
        });
    }

    public void filterAndDisplay() {
        int monthPosition = spinnerMonth.getSelectedItemPosition();
        String keyword = edtSearch.getText().toString();
        String yearText = edtYear.getText().toString();

        viewModel.filter(this, monthPosition, keyword, yearText);
        expenseAdapter.setData(viewModel.filteredExpenses);

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        incomeStr = formatter.format(viewModel.totalIncome);
        expenseStr = formatter.format(viewModel.totalExpense);
        differenceStr = formatter.format(viewModel.difference);
    }
}





