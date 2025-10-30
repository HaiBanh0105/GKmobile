package com.example.gk;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.gk.Database.ExpenseDAO;

@Entity(tableName = "exchange_rates")
public class ExchangeRate {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String baseCurrency;   // ví dụ: "USD"
    public String targetCurrency; // ví dụ: "VND"
    public double rate;           // ví dụ: 26331.5
    public long lastUpdated;


    @Ignore
    public ExchangeRate(String baseCurrency, String targetCurrency, double rate, long lastUpdated) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.lastUpdated = lastUpdated;
    }

    public ExchangeRate(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

