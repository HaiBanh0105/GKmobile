package com.example.gk.viewmodel;

import android.content.Context;

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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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


        // Pie chart
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            float convertedAmount = (float) (entry.getValue() * rateFromVND);
            pieEntries.add(new PieEntry(convertedAmount, entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextSize(14f);
        pieDataSet.setValueTextColor(android.graphics.Color.WHITE);
        pieData = new PieData(pieDataSet);

        // Bar chart
        List<BarEntry> incomeEntries = List.of(new BarEntry(0, (float) convertedIncome));
        List<BarEntry> expenseEntries = List.of(new BarEntry(1, (float) convertedExpense));

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Thu nhập");
        incomeSet.setColor(android.graphics.Color.parseColor("#4CAF50"));
        incomeSet.setDrawValues(false);

        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Chi tiêu");
        expenseSet.setColor(android.graphics.Color.parseColor("#F44336"));
        expenseSet.setDrawValues(false);

        barData = new BarData(incomeSet, expenseSet);
    }
}
