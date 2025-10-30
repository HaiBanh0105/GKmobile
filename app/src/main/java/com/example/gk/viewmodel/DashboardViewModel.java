package com.example.gk.viewmodel;

import android.content.Context;
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

    public double totalIncome = 0;
    public double totalExpense = 0;
    public double difference = 0;

    public PieData pieData;
    public BarData barData;

    public void loadData(Context context, String yearStr, int monthIndex) {
        ExpenseDAO expenseDAO = AppDatabase.getInstance(context).expenseDAO();
        ExchangeDAO exchangeDAO = AppDatabase.getInstance(context).exchangeDAO();

        List<Expense> filteredList = (monthIndex == 0)
                ? expenseDAO.getExpensesByYearOnly(yearStr)
                : expenseDAO.getExpensesByYearAndMonth(yearStr, String.format("%02d", monthIndex));

        totalIncome = 0;
        totalExpense = 0;
        difference = 0;

        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense e : filteredList) {
            ExchangeRate rate = exchangeDAO.getRateByCurrency(e.currency);
            double rateToVND = (rate != null) ? rate.rateToVND : 1.0;
            double amountVND = e.amount * rateToVND;

            if (e.isIncome) {
                totalIncome += amountVND;
            } else {
                totalExpense += amountVND;
                String category = e.category != null ? e.category : "Khác";
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amountVND);
            }
        }

        difference = totalIncome - totalExpense;

        // Pie chart
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextSize(14f);
        pieDataSet.setValueTextColor(android.graphics.Color.WHITE);
        pieData = new PieData(pieDataSet);

        // Bar chart
        List<BarEntry> incomeEntries = List.of(new BarEntry(0, (float) totalIncome));
        List<BarEntry> expenseEntries = List.of(new BarEntry(1, (float) totalExpense));

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Thu nhập");
        incomeSet.setColor(android.graphics.Color.parseColor("#4CAF50"));
        incomeSet.setDrawValues(false);

        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Chi tiêu");
        expenseSet.setColor(android.graphics.Color.parseColor("#F44336"));
        expenseSet.setDrawValues(false);

        barData = new BarData(incomeSet, expenseSet);
    }
}
