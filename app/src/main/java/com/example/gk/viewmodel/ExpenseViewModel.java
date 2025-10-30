package com.example.gk.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;


import com.example.gk.BR;
import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.example.gk.ExchangeRate;
import com.example.gk.Expense;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

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

    public void saveExpense(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String baseCurrency = expense.getCurrency(); // đơn vị người dùng nhập
            String targetCurrency = "VND";               // đơn vị mặc định để lưu
            double originalAmount = expense.getAmount();

            ExchangeDAO dao = AppDatabase.getInstance(context).exchangeDAO();
            ExchangeRate rate = dao.getRate(baseCurrency, targetCurrency);

            boolean needUpdate = rate == null || System.currentTimeMillis() - rate.lastUpdated > 24 * 60 * 60 * 1000;

            if (needUpdate) {
                try {
                    String accessKey = "a666cc608cd1a35a2dcde0629a910b48";
                    String url = "https://api.exchangerate.host/convert?from=" + baseCurrency + "&to=" + targetCurrency + "&amount=1&access_key=" + accessKey;

                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());

                    if (!json.optBoolean("success", false)) {
                        Log.e("ExchangeRate", "API lỗi: " + json.optJSONObject("error"));
                        return;
                    }

                    JSONObject info = json.optJSONObject("info");
                    if (info == null || !info.has("quote")) {
                        Log.e("ExchangeRate", "Không có 'quote' trong phản hồi: " + json.toString());
                        return;
                    }

                    double rateValue = info.getDouble("quote");

                    // Lưu tỷ giá mới
                    rate = new ExchangeRate();
                    rate.baseCurrency = baseCurrency;
                    rate.targetCurrency = targetCurrency;
                    rate.rate = rateValue;
                    rate.lastUpdated = System.currentTimeMillis();
                    dao.insert(rate);

                } catch (Exception e) {
                    Log.e("ExchangeRate", "Lỗi gọi API: " + e.getMessage(), e);
                    return;
                }
            }

            // Quy đổi sang VND
            double convertedAmount = originalAmount * rate.rate;
            expense.setAmount(convertedAmount);

            // Lưu giao dịch
            AppDatabase.getInstance(context).expenseDAO().insertExpense(expense);
        });
    }






}
