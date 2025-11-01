package com.example.gk.viewmodel;

import android.content.Context;
import android.graphics.Color;

import com.example.gk.AppConstants;
import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.example.gk.Database.ExpenseDAO;
import com.example.gk.ExchangeRate;
import com.example.gk.Expense;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardViewModel {

    public double convertedIncome = 0;
    public double convertedExpense = 0;
    public double convertedDifference = 0;

    public PieData pieData;
    public BarData barData;

    public void loadData(Context context, String yearStr, int monthIndex) {
        ExpenseDAO expenseDAO = AppDatabase.getInstance(context).expenseDAO();
        ExchangeDAO exchangeDAO = AppDatabase.getInstance(context).exchangeDAO();
        ExchangeRate rate = exchangeDAO.getRate(AppConstants.currentCurrency,"VND");
        double rateFromVND = (rate != null && rate.rate > 0) ? rate.rate : 1.0;


        List<Expense> filteredList = (monthIndex == 0)
                ? expenseDAO.getExpensesByYearOnly(yearStr)
                : expenseDAO.getExpensesByYearAndMonth(yearStr, String.format("%02d", monthIndex));

        double totalIncome = 0;
        double totalExpense = 0;
        double difference = 0;

        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense e : filteredList) {
            double amountVND = e.amount; // đã được quy đổi khi lưu

            if (e.isIncome) {
                totalIncome += amountVND;
            } else {
                totalExpense += amountVND;

                String category = (e.category != null && !e.category.isEmpty()) ? e.category : "Khác";
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amountVND);
            }
        }

        convertedIncome = totalIncome / rateFromVND;
        convertedExpense = totalExpense / rateFromVND;
        convertedDifference = convertedIncome - convertedExpense;


        List<Integer> customColors = Arrays.asList(
                Color.parseColor("#4CAF50"), // xanh lá
                Color.parseColor("#FF9800"), // cam
                Color.parseColor("#F44336"), // đỏ
                Color.parseColor("#2196F3"), // xanh dương
                Color.parseColor("#9C27B0"), // tím
                Color.parseColor("#00BCD4"), // xanh ngọc
                Color.parseColor("#795548"), // nâu
                Color.parseColor("#607D8B"), // xám
                Color.parseColor("#E91E63"), // hồng
                Color.parseColor("#8BC34A")  // xanh lá nhạt
                // thêm màu nếu cần
        );

        pieData = generatePieData(categoryTotals, customColors);
        barData = generateBarData((float) convertedIncome, (float) convertedExpense);
    }

    public PieData generatePieData(Map<String, Double> categoryTotals, List<Integer> customColors) {
        List<PieEntry> pieEntries = new ArrayList<>();
        double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            float percent = (float) ((entry.getValue() / total) * 100);
            String labelWithPercent = entry.getKey() + " (" + String.format(Locale.US, "%.1f", percent) + "%)";
            pieEntries.add(new PieEntry(entry.getValue().floatValue(), labelWithPercent));

        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(customColors);
        pieDataSet.setValueTextSize(14f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setSliceSpace(2f);


        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);
        pieData.setValueFormatter(new PercentFormatter());

        return pieData;
    }

    public BarData generateBarData(float income, float expense) {
        List<BarEntry> incomeEntries = List.of(new BarEntry(0, income));
        List<BarEntry> expenseEntries = List.of(new BarEntry(1, expense));

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Thu nhập");
        incomeSet.setColor(Color.parseColor("#4CAF50"));
        incomeSet.setDrawValues(false);

        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Chi tiêu");
        expenseSet.setColor(Color.parseColor("#F44336"));
        expenseSet.setDrawValues(false);

        return new BarData(incomeSet, expenseSet);
    }


}
