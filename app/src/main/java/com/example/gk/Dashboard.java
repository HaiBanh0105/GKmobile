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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.example.gk.Database.ExpenseDAO;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class Dashboard extends BaseActivity {
    TextView tvTotalIncome;
    TextView tvTotalExpense;

    TextView tvDifference;
    Spinner spinnerMonth;
    EditText edtYear;
    PieChart pieChart;
    BarChart barChart;
    MaterialToolbar toolbar;
    FloatingActionButton AddExpense;
    FloatingActionButton Export;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dashboard);

        setupToolbar(R.id.toolbarDashboard);

        initUi();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR); //Lấy năm hiện tại
        edtYear.setText(String.valueOf(currentYear));

        pieChart.setDescription(null);

        barChart.setDescription(null);

        AddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, add_expense.class);
                startActivity(intent);
            }
        });



        String[] months = {"Không chọn", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6"
                , "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11","Tháng 12"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1; //lấy tháng hiện tại
        spinnerMonth.setSelection(currentMonth);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadSummaryInVND();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        edtYear.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { loadSummaryInVND(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        Export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String yearText = edtYear.getText().toString().trim();
                int monthText = spinnerMonth.getSelectedItemPosition();

                if (yearText.isEmpty()) {
                    Toast.makeText(Dashboard.this, "Vui lòng nhập năm trước khi xuất báo cáo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (monthText == 0) {
                    Toast.makeText(Dashboard.this, "Vui lòng chọn tháng trước khi xuất báo cáo", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Dashboard.this, export_report.class);
                intent.putExtra("month", monthText);
                intent.putExtra("year", edtYear.getText().toString().trim());
                intent.putExtra("totalIncome", tvTotalIncome.getText().toString().trim());
                intent.putExtra("totalExpense", tvTotalExpense.getText().toString().trim());
                intent.putExtra("Difference", tvDifference.getText().toString().trim());
                startActivity(intent);
            }
        });
    }

    private void initUi(){
        edtYear = findViewById(R.id.edtYear);

        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvDifference = findViewById(R.id.tvDifference);


        pieChart = findViewById(R.id.pieChart);


        barChart = findViewById(R.id.barChart);
        AddExpense = findViewById(R.id.btnAddExpense);
        Export = findViewById(R.id.btnExportPdf);

        toolbar = findViewById(R.id.toolbarDashboard);

        spinnerMonth = findViewById(R.id.spinnerMonth);

    }

    private void updatePieChartByCategory(ExpenseDAO expenseDAO, ExchangeDAO exchangeDAO, String yearStr, int selectedMonth) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Expense> all = expenseDAO.getExpensesByYearOnly(yearStr);

            Map<String, Double> categoryTotals = new HashMap<>();

            for (Expense e : all) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(e.timestamp);
                int month = cal.get(Calendar.MONTH) + 1;

                if (month != selectedMonth) continue;
                if (e.isIncome) continue;

                ExchangeRate rate = exchangeDAO.getRateByCurrency(e.currency);
                double rateToVND = (rate != null) ? rate.rateToVND : 1.0;
                double amountVND = e.amount * rateToVND;

                String category = e.category != null ? e.category : "Khác";
                double currentTotal = categoryTotals.getOrDefault(category, 0.0);
                categoryTotals.put(category, currentTotal + amountVND);
            }

            List<PieEntry> pieEntries = new ArrayList<>();
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                pieEntries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }

            PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
            pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            pieDataSet.setValueTextSize(14f);
            pieDataSet.setValueTextColor(Color.WHITE);

            PieData pieData = new PieData(pieDataSet);

            pieChart.setData(pieData);
            pieChart.setUsePercentValues(true);
            pieChart.setEntryLabelColor(Color.BLACK);
            pieChart.setCenterText("Tỉ lệ chi tiêu");
            pieChart.setCenterTextSize(16f);
            pieChart.setDrawEntryLabels(false);

            Description description = new Description();
            description.setEnabled(false);
            pieChart.setDescription(description);

            Legend legend = pieChart.getLegend();
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setOrientation(Legend.LegendOrientation.VERTICAL);
            legend.setDrawInside(false);
            legend.setTextSize(15f);

            pieChart.invalidate();
        });
    }




    private void updateBarChart(ExpenseDAO expenseDAO, ExchangeDAO exchangeDAO, String yearStr, int selectedMonth) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Expense> all = expenseDAO.getExpensesByYearOnly(yearStr);

            double incomeTotal = 0;
            double expenseTotal = 0;

            for (Expense e : all) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(e.timestamp);
                int month = cal.get(Calendar.MONTH) + 1; // 1–12

                if (month != selectedMonth) continue;

                ExchangeRate rate = exchangeDAO.getRateByCurrency(e.currency);
                double rateToVND = (rate != null) ? rate.rateToVND : 1.0;
                double amountVND = e.amount * rateToVND;

                if (e.isIncome) {
                    incomeTotal += amountVND;
                } else {
                    expenseTotal += amountVND;
                }
            }

            List<BarEntry> incomeEntries = new ArrayList<>();
            List<BarEntry> expenseEntries = new ArrayList<>();

            incomeEntries.add(new BarEntry(0, (float) incomeTotal));
            expenseEntries.add(new BarEntry(1, (float) expenseTotal));

            BarDataSet incomeSet = new BarDataSet(incomeEntries, "Thu nhập");
            incomeSet.setColor(Color.parseColor("#4CAF50"));

            BarDataSet expenseSet = new BarDataSet(expenseEntries, "Chi tiêu");
            expenseSet.setColor(Color.parseColor("#F44336"));

            BarData barData = new BarData(incomeSet, expenseSet);
            incomeSet.setDrawValues(false);
            expenseSet.setDrawValues(false);


            Legend legend = barChart.getLegend();
            legend.setTextSize(14f);
            legend.setTextColor(Color.DKGRAY);

            barChart.setData(barData);
            barChart.getXAxis().setGranularity(1f);
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barChart.getXAxis().setCenterAxisLabels(true);
            barChart.getXAxis().setAxisMinimum(-0.5f);
            barChart.getXAxis().setAxisMaximum(2f);
            barChart.groupBars(0f, 0.2f, 0.02f);

            barChart.getXAxis().setEnabled(false);
            barChart.getAxisLeft().setEnabled(false);
            barChart.getAxisRight().setEnabled(false);

            barChart.invalidate();
        });
    }


    private void loadSummaryInVND() {
        final String yearStr = edtYear.getText().toString().trim();
        final int monthIndex = spinnerMonth.getSelectedItemPosition();

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
            final double difference = totalIncomeVND - totalExpenseVND;

            runOnUiThread(() -> {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                String incomeStr = formatter.format(totalIncomeVND);
                String expenseStr = formatter.format(totalExpenseVND);
                String differenceStr = formatter.format(difference);

                tvTotalIncome.setText("Thu: " + incomeStr + " VND");
                tvTotalExpense.setText("Chi: " + expenseStr + " VND");
                tvDifference.setText("Còn lại: " + differenceStr + " VND");
            });

            updatePieChartByCategory(expenseDAO, exchangeDAO, yearStr, monthIndex);
            updateBarChart(expenseDAO, exchangeDAO, yearStr, monthIndex);

        });
    }

}