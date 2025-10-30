package com.example.gk.viewmodel;

import android.content.Context;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.example.gk.Database.ExpenseDAO;
import com.example.gk.ExchangeRate;
import com.example.gk.Expense;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatisticsViewModel {

    public List<Expense> allExpenses = new ArrayList<>();
    public List<Expense> filteredExpenses = new ArrayList<>();

    public double totalIncome = 0;
    public double totalExpense = 0;
    public double difference = 0;

    public void loadAllExpenses(Context context) {
        ExpenseDAO expenseDAO = AppDatabase.getInstance(context).expenseDAO();
        allExpenses = expenseDAO.getListExpense();
    }

    public void filter(Context context, int monthPosition, String keyword, String yearText) {
        filteredExpenses.clear();

        for (Expense expense : allExpenses) {
            boolean matchMonth = (monthPosition == 0);
            boolean matchKeyword = keyword.isEmpty();
            boolean matchYear = yearText.isEmpty();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(expense.getTimestamp());
            int expenseMonth = calendar.get(Calendar.MONTH) + 1;
            int expenseYear = calendar.get(Calendar.YEAR);

            if (!matchMonth) matchMonth = (expenseMonth == monthPosition);
            if (!matchKeyword) {
                String title = expense.getTitle().toLowerCase();
                String category = expense.getCategory().toLowerCase();
                matchKeyword = title.contains(keyword.toLowerCase()) || category.contains(keyword.toLowerCase());
            }
            if (!matchYear) {
                try {
                    int inputYear = Integer.parseInt(yearText);
                    matchYear = (expenseYear == inputYear);
                } catch (NumberFormatException e) {
                    matchYear = true;
                }
            }

            if (matchMonth && matchKeyword && matchYear) {
                filteredExpenses.add(expense);
            }
        }

        calculateSummary(context);
    }

    private void calculateSummary(Context context) {
        totalIncome = 0;
        totalExpense = 0;
        difference = 0;

        ExchangeDAO exchangeDAO = AppDatabase.getInstance(context).exchangeDAO();

        for (Expense e : filteredExpenses) {
            ExchangeRate rate = exchangeDAO.getRateByCurrency(e.currency);
            double rateToVND = (rate != null) ? rate.rateToVND : 1.0;
            double amountVND = e.amount * rateToVND;

            if (e.isIncome) {
                totalIncome += amountVND;
            } else {
                totalExpense += amountVND;
            }
        }

        difference = totalIncome - totalExpense;
    }
}
