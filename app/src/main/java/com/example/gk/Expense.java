package com.example.gk;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "expenses")
public class Expense implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;           // Tiêu đề (ví dụ: "Ăn sáng", "Lương")
    public double amount;          // Số tiền
    public String currency;        // Đơn vị tiền tệ (VND, USD...)
    public String category;        // Loại (Ăn uống, Giải trí, Lương...)
    public boolean isIncome;       // true nếu là thu nhập, false nếu là chi tiêu
    public long timestamp;         // Thời gian (dạng millis)

    public Expense() {
        // Constructor rỗng cho Room
    }
    @Ignore
    public Expense(String title, double amount, String currency, String category, boolean isIncome, long timestamp) {
        this.title = title;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.isIncome = isIncome;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
