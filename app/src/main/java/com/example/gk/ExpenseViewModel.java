package com.example.gk;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ExpenseViewModel extends BaseObservable {

    private Expense expense;

    public ExpenseViewModel(Expense expense) {
        this.expense = expense;
    }

    public Expense getExpense() {
        return expense;
    }


    @Bindable
    public String getTitle() {
        return expense.getTitle();
    }

    public void setTitle(String title) {
        expense.setTitle(title);
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getAmount() {
        return String.valueOf(expense.getAmount());
    }

    public void setAmount(String amountStr) {
        double value = Double.parseDouble(amountStr);
        expense.setAmount(value);
        notifyPropertyChanged(BR.amount);
    }

    @Bindable
    public String getCurrency()
    {
        return expense.getCurrency();
    }

    public void setCurrency(String currency) {
        expense.setCurrency(currency);
        notifyPropertyChanged(BR.currency);
    }

    @Bindable
    public String getCategory() {
        return expense.getCategory();
    }

    public void setCategory(String category) {
        expense.setCategory(category);
        notifyPropertyChanged(BR.category);
    }

    @Bindable
    public boolean isIncome() {
        return expense.isIncome;
    }

    public void setIncome(boolean income) {
        expense.setIncome(income);
        notifyPropertyChanged(BR.income);
    }


    @Bindable
    public long getTimestamp() {
        return expense.getTimestamp();
    }

    public void setTimestamp(long timestamp) {
        expense.setTimestamp(timestamp);
        notifyPropertyChanged(BR.timestamp);

    }

}
