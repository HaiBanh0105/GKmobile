package com.example.gk.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.gk.BR;
import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.example.gk.Database.ExpenseDAO;
import com.example.gk.ExchangeRate;
import com.example.gk.Expense;
import com.google.firebase.firestore.FirebaseFirestore;

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
    public String getAmountIn() {
        return String.valueOf(expense.getAmountIn());
    }

    public void setAmountIn(String amountStr) {
        double value = Double.parseDouble(amountStr);
        expense.setAmountIn(value);
        notifyPropertyChanged(BR.amountIn);
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
            double originalAmount = expense.getAmountIn(); //quy đổi về vnd để tính tổng

            ExchangeDAO dao = AppDatabase.getInstance(context).exchangeDAO();
            ExchangeRate rate = null; // Khởi tạo null

            if (baseCurrency.equalsIgnoreCase(targetCurrency)) {
                // Tạo một tỷ giá giả định là 1.0
                rate = new ExchangeRate();
                rate.baseCurrency = baseCurrency;
                rate.targetCurrency = targetCurrency;
                rate.rate = 1.0;
                // Không cần lastUpdated vì tỷ giá 1:1 không bao giờ đổi
            } else {
                // Nếu khác nhau (VD: USD -> VND) thì mới làm theo quy trin
                rate = dao.getLatestRate(baseCurrency, targetCurrency);

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
                            // Nếu API lỗi và không có rate cũ, không thể lưu được -> return
                            if (rate == null) return;
                        } else {
                            JSONObject info = json.optJSONObject("info");
                            if (info != null && info.has("quote")) {
                                double rateValue = info.getDouble("quote");

                                // Cập nhật hoặc tạo mới rate
                                if (rate == null) {
                                    rate = new ExchangeRate();
                                    rate.baseCurrency = baseCurrency;
                                    rate.targetCurrency = targetCurrency;
                                }
                                rate.rate = rateValue;
                                rate.lastUpdated = System.currentTimeMillis();

                                dao.insert(rate);

                                // Đồng bộ Firestore
                                ExchangeRate latestExchange = dao.getLatestExchange();
                                if (latestExchange != null) {
                                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                    firestore.collection("exchange_rate")
                                            .document(String.valueOf(latestExchange.getId()))
                                            .set(latestExchange);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ExchangeRate", "Lỗi gọi API: " + e.getMessage(), e);
                        // Nếu lỗi mạng và không có rate cũ, dùng tạm 1.0 để không crash app
                        if (rate == null) {
                            rate = new ExchangeRate();
                            rate.rate = 1.0;
                        }
                    }
                }
            }
            // --------------------------------------------------------------------------------

            // Kiểm tra null lần cuối để tránh Crash ---
            double finalRate = (rate != null) ? rate.rate : 1.0;

            // Quy đổi sang VND
            double convertedAmount = originalAmount * finalRate;
            expense.setAmount(convertedAmount);
            expense.setAmountIn(expense.getAmountIn()); // Giữ nguyên số tiền nhập

            // Lưu giao dịch vào Room
            ExpenseDAO expenseDAO = AppDatabase.getInstance(context).expenseDAO();
            expenseDAO.insertExpense(expense);

            // Đồng bộ Firestore (Giữ nguyên code của bạn)
            Expense latestExpense = expenseDAO.getLatestExpense();
            if (latestExpense != null) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("expenses")
                        .document(String.valueOf(latestExpense.getId()))
                        .set(latestExpense)
                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Đã đồng bộ Expense: " + latestExpense.getId()))
                        .addOnFailureListener(e -> Log.e("Firestore", "Lỗi đồng bộ Expense", e));
            }
        });
    }
    public boolean isValid() {
        if (expense.getTitle() == null || expense.getTitle().trim().isEmpty()) return false;
        if (expense.getAmountIn() <= 0) return false;
        if (expense.getCurrency() == null || expense.getCurrency().trim().isEmpty()) return false;
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) return false;
        return true;
    }

}
