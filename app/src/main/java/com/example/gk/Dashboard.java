package com.example.gk;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.example.gk.Database.ExpenseDAO;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class Dashboard extends AppCompatActivity {

    TextView tvTotalIncome;
    TextView tvTotalExpense;
    Spinner spinnerMonth;
    EditText edtYear;

    PieChart pieChartExpense;
    PieChart pieChartIncome;
    BarChart barChart;


    FloatingActionButton AddExpense;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dashboard);

        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        pieChartExpense = findViewById(R.id.pieChartExpense);
        pieChartIncome = findViewById(R.id.pieChartIncome);

        barChart = findViewById(R.id.barChart);

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

        spinnerMonth = findViewById(R.id.spinnerMonth);
        String[] months = {"Không chọn", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6"
                , "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11","Tháng 12"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadSummaryInVND();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        edtYear = findViewById(R.id.edtYear);
        edtYear.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { loadSummaryInVND(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }


    private void updatePieChartByCategory(List<Expense> expenses, ExchangeDAO exchangeDAO, boolean isIncome, PieChart chart) {
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense e : expenses) {
            if (e.isIncome == isIncome) {
                ExchangeRate rate = exchangeDAO.getRateByCurrency(e.currency);
                double rateToVND = (rate != null) ? rate.rateToVND : 1.0;
                double amountVND = e.amount * rateToVND;

                categoryTotals.put(e.category,
                        categoryTotals.getOrDefault(e.category, 0.0) + amountVND);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, isIncome ? "Thu nhập" : "Chi tiêu");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.setEntryLabelTextSize(12f);
        chart.setCenterText(isIncome ? "Thu nhập" : "Chi tiêu");
        chart.setCenterTextSize(16f);
        chart.invalidate();
    }


    private void updateBarChart(ExpenseDAO expenseDAO, ExchangeDAO exchangeDAO, String yearStr) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Expense> all = expenseDAO.getExpensesByYearOnly(yearStr);

            double[] incomeByMonth = new double[12];
            double[] expenseByMonth = new double[12];

            for (Expense e : all) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(e.timestamp);
                int month = cal.get(Calendar.MONTH); // 0–11

                ExchangeRate rate = exchangeDAO.getRateByCurrency(e.currency);
                double rateToVND = (rate != null) ? rate.rateToVND : 1.0;
                double amountVND = e.amount * rateToVND;

                if (e.isIncome) {
                    incomeByMonth[month] += amountVND;
                } else {
                    expenseByMonth[month] += amountVND;
                }
            }

            List<BarEntry> incomeEntries = new ArrayList<>();
            List<BarEntry> expenseEntries = new ArrayList<>();

            for (int i = 0; i < 12; i++) {
                incomeEntries.add(new BarEntry(i, (float) incomeByMonth[i]));
                expenseEntries.add(new BarEntry(i, (float) expenseByMonth[i]));
            }

            BarDataSet incomeSet = new BarDataSet(incomeEntries, "Thu nhập");
            incomeSet.setColor(Color.parseColor("#4CAF50"));

            BarDataSet expenseSet = new BarDataSet(expenseEntries, "Chi tiêu");
            expenseSet.setColor(Color.parseColor("#F44336"));

            BarData barData = new BarData(incomeSet, expenseSet);
            barData.setBarWidth(0.4f);

            barChart.setData(barData);
            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(
                    Arrays.asList("T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12")));
            barChart.getXAxis().setGranularity(1f);
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barChart.groupBars(0f, 0.2f, 0.02f);
            barChart.invalidate();
        });
    }


    private void loadSummaryInVND() {
        final String yearStr = edtYear.getText().toString().trim();
        final int monthIndex = spinnerMonth.getSelectedItemPosition(); // 0 = "Không chọn"

        if (yearStr.isEmpty()) return;

        final String monthStr = String.format("%02d", monthIndex);

        Executors.newSingleThreadExecutor().execute(() -> {
            ExpenseDAO expenseDAO = AppDatabase.getInstance(Dashboard.this).expenseDAO();
            ExchangeDAO exchangeDAO = AppDatabase.getInstance(Dashboard.this).exchangeDAO();

            List<Expense> filteredList;
            if (monthIndex == 0) {
                filteredList = expenseDAO.getExpensesByYearOnly(yearStr);
            } else {
                filteredList = expenseDAO.getExpensesByYearAndMonth(yearStr, monthStr);
            }

            double income = 0;
            double expense = 0;

            for (Expense e : filteredList) {
                ExchangeRate rate = exchangeDAO.getRateByCurrency(e.currency);
                double rateToVND = (rate != null) ? rate.rateToVND : 1.0;
                double amountVND = e.amount * rateToVND;

                if (e.isIncome) {
                    income += amountVND;
                } else {
                    expense += amountVND;
                }
            }

            final double totalIncomeVND = income;
            final double totalExpenseVND = expense;

            runOnUiThread(() -> {
                tvTotalIncome.setText("Thu: " + String.format("%.0f VND", totalIncomeVND));
                tvTotalExpense.setText("Chi: " + String.format("%.0f VND", totalExpenseVND));
            });

            updatePieChartByCategory(filteredList, exchangeDAO, false, pieChartExpense); // Chi tiêu
            updatePieChartByCategory(filteredList, exchangeDAO, true, pieChartIncome);   // Thu nhập

            updateBarChart(expenseDAO, exchangeDAO, yearStr);

        });
    }

}