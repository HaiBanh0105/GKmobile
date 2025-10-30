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

import com.example.gk.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class Dashboard extends BaseActivity {

    private TextView tvTotalIncome, tvTotalExpense, tvDifference;
    private Spinner spinnerMonth;
    private EditText edtYear;
    private PieChart pieChart;
    private BarChart barChart;
    private FloatingActionButton AddExpense, Export;
    private DashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        setupToolbar(R.id.toolbarDashboard);
        initUi();

        viewModel = new DashboardViewModel();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        edtYear.setText(String.valueOf(currentYear));

        setupChartDefaults();
        setupMonthSpinner();
        setupYearWatcher();
        setupButtons();

        loadSummaryInVND(); // initial load
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
        Export = findViewById(R.id.btnExportPdf);
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
                loadSummaryInVND();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupYearWatcher() {
        edtYear.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { loadSummaryInVND(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void setupButtons() {
        AddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Add_expense.class);
            startActivity(intent);
        });

        Export.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng xuất PDF đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSummaryInVND() {
        String yearStr = edtYear.getText().toString().trim();
        int monthIndex = spinnerMonth.getSelectedItemPosition();
        if (yearStr.isEmpty()) return;

        viewModel.loadData(this, yearStr, monthIndex);

        // Hiển thị số liệu
        tvTotalIncome.setText("Thu: " + formatVND(viewModel.totalIncome));
        tvTotalExpense.setText("Chi: " + formatVND(viewModel.totalExpense));
        tvDifference.setText("Còn lại: " + formatVND(viewModel.difference));

        // Hiển thị biểu đồ
        pieChart.setData(viewModel.pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setCenterText("Tỉ lệ chi tiêu");
        pieChart.setCenterTextSize(16f);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();

        barChart.setData(viewModel.barData);
        barChart.getXAxis().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    private String formatVND(double value) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(value) + " VND";
    }
}
