package com.example.gk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.CurrencyDAO;
import com.example.gk.Database.ExchangeDAO;
import com.example.gk.Repository.EchangeRepository;
import com.example.gk.Repository.ExpenseRepository;
import com.example.gk.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class Dashboard extends BaseActivity {

    private TextView tvTotalIncome, tvTotalExpense, tvDifference;
    private Spinner spinnerMonth;
    private EditText edtYear;
    private PieChart pieChart;
    private BarChart barChart;
    private FloatingActionButton AddExpense, Statistics;
    private DashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        //Đồng bộ firebase về Room
        ExpenseRepository expenseRepository = new ExpenseRepository(this);
        expenseRepository.syncFromFirestore();

        EchangeRepository echangeRepository = new EchangeRepository(this);
        echangeRepository.syncExchangeRatesFromFirestore();

        setupToolbar(R.id.toolbarDashboard);
        initDefaultCurrenciesIfNeeded(this);
        initUi();

        viewModel = new DashboardViewModel();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        edtYear.setText(String.valueOf(currentYear));

        setupChartDefaults();
        setupMonthSpinner();
        setupYearWatcher();
        setupButtons();

        loadSummary(); // initial load
    }

    private void initUi() {
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvDifference = findViewById(R.id.tvDifference);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        edtYear = findViewById(R.id.edtYear);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        AddExpense = findViewById(R.id.btnAddExpense);
        Statistics = findViewById(R.id.btnStatistics);
    }

    private void setupChartDefaults() {
        pieChart.setDescription(null);
        barChart.setDescription(null);

        Legend pieLegend = pieChart.getLegend();
        pieLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        pieLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        pieLegend.setOrientation(Legend.LegendOrientation.VERTICAL);
        pieLegend.setDrawInside(false);
        pieLegend.setTextSize(15f);

        Legend barLegend = barChart.getLegend();
        barLegend.setTextSize(14f);
        barLegend.setTextColor(Color.DKGRAY);
    }

    private void setupMonthSpinner() {
        String[] months = {"Không chọn", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        spinnerMonth.setSelection(currentMonth);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadSummary();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupYearWatcher() {
        edtYear.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { loadSummary(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void setupButtons() {
        AddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Add_expense.class);
            startActivity(intent);
        });

        Statistics.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Statistics.class);
            startActivity(intent);
        });

        FloatingActionButton btnCurrencyDialog = findViewById(R.id.btnCurrencyDialog);
        btnCurrencyDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrencyConverterDialog(Dashboard.this);
            }
        });

    }

    public void loadSummary() {
        String yearStr = edtYear.getText().toString().trim();
        int monthIndex = spinnerMonth.getSelectedItemPosition();
        if (yearStr.isEmpty()) return;

        viewModel.loadData(this, yearStr, monthIndex);

        updateSummaryText();
        setupPieChart(viewModel.pieData);
        setupBarChart(viewModel.barData, (float) viewModel.convertedIncome, (float) viewModel.convertedExpense);
    }

    private void updateSummaryText() {
        tvTotalIncome.setText(formatCurrency(viewModel.convertedIncome));
        tvTotalExpense.setText(formatCurrency(viewModel.convertedExpense));
        tvDifference.setText(formatCurrency(viewModel.convertedDifference));
    }

    private void setupPieChart(PieData data) {
        pieChart.setUsePercentValues(false);
        data.setDrawValues(false);

        pieChart.setData(data);
        pieChart.setCenterText("Tỉ lệ chi tiêu");
        pieChart.setCenterTextSize(16f);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
        pieChart.animateY(1000);
    }
    private void setupBarChart(BarData data, float income, float expense) {
        barChart.setData(data);

        barChart.getXAxis().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);

        float maxValue = Math.max(income, expense);
        barChart.getAxisLeft().setAxisMaximum(maxValue * 1.1f);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawLabels(true);

        barChart.invalidate();
        barChart.animateY(1000);

    }




    private String formatCurrency(double value) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(value) + " " + AppConstants.currentCurrency;
    }



    private void initDefaultCurrenciesIfNeeded(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            CurrencyDAO dao = AppDatabase.getInstance(context).currencyDAO();
            List<CurrencyInfo> existing = dao.getAllCurrencies();

            if (existing == null || existing.isEmpty()) {
                List<CurrencyInfo> defaultCurrencies = Arrays.asList(
                        new CurrencyInfo("USD", "Đô la Mỹ"),
                        new CurrencyInfo("EUR", "Euro"),
                        new CurrencyInfo("JPY", "Yên Nhật"),
                        new CurrencyInfo("GBP", "Bảng Anh"),
                        new CurrencyInfo("AUD", "Đô la Úc"),
                        new CurrencyInfo("VND", "Đồng Việt Nam")
                );

                for (CurrencyInfo currency : defaultCurrencies) {
                    dao.insert(currency);
                }
            }
        });
    }


    public void showCurrencyConverterDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.activity_currency_converter, null);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        EditText amountInput = dialogView.findViewById(R.id.amountInput);
        TextView resultText = dialogView.findViewById(R.id.resultText);
        Spinner spinnerFrom = dialogView.findViewById(R.id.spinnerFromCurrency);
        Spinner spinnerTo = dialogView.findViewById(R.id.spinnerToCurrency);
        ImageView swap = dialogView.findViewById(R.id.btnSwap);
        Button close = dialogView.findViewById(R.id.btnCloseDialog);

        ExchangeDAO dao = AppDatabase.getInstance(this).exchangeDAO();
        List<String> currencyList = dao.getAllBaseCurrencies();
        // Thêm "VND" nếu chưa có
        if (!currencyList.contains("VND")) {
            currencyList.add("VND");
        }
        String[] currencies = currencyList.toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, currencies);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Hàm cập nhật kết quả
        Runnable updateResult = () -> {
            try {
                String Currency_In = spinnerFrom.getSelectedItem().toString();
                String Currency_out = spinnerTo.getSelectedItem().toString();

                ExchangeRate rateIn_VND = dao.getLatestRate(Currency_In, "VND");
                ExchangeRate rateOut_VND = dao.getLatestRate(Currency_out, "VND");

                double rateInValue = rateIn_VND.getRate();
                double rateOutValue = rateOut_VND.getRate();

                String amountStr = amountInput.getText().toString();
                double amountEntered = 0;
                if (!amountStr.isEmpty()) {
                    amountEntered = Double.parseDouble(amountStr);
                }

                double amount = (rateInValue / rateOutValue) * amountEntered;
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                formatter.setMaximumFractionDigits(2);
                String formattedAmount = formatter.format(amount);
                resultText.setText(formattedAmount);

            } catch (Exception e) {
                resultText.setText("");
            }
        };

        // Bắt sự kiện nhập số
        amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateResult.run();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Bắt sự kiện thay đổi đơn vị
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateResult.run();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateResult.run();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy vị trí hiện tại của 2 spinner
                int fromPos = spinnerFrom.getSelectedItemPosition();
                int toPos = spinnerTo.getSelectedItemPosition();

                // Hoán đổi vị trí
                spinnerFrom.setSelection(toPos);
                spinnerTo.setSelection(fromPos);
                updateResult.run();
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Tùy chọn: Chỉnh độ rộng dialog (ví dụ 90% màn hình)
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupChartDefaults();
        setupMonthSpinner();
        setupYearWatcher();
        setupButtons();
        loadSummary();
    }



}
