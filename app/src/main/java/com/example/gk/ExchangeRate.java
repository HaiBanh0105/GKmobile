package com.example.gk;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.gk.Database.ExpenseDAO;

@Entity(tableName = "exchange_rates")
public class ExchangeRate {
    @PrimaryKey
    @NonNull
    public String currencyCode;    // Ví dụ: "USD", "EUR"

    public double rateToVND;       // Tỷ giá quy đổi sang VND
    public long lastUpdated;       // Thời điểm cập nhật


    public ExchangeRate(){

    }

    public ExchangeRate(@NonNull String currencyCode, double rateToVND, long lastUpdated) {
        this.currencyCode = currencyCode;
        this.rateToVND = rateToVND;
        this.lastUpdated = lastUpdated;
    }
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public double getRateToVND() {
        return rateToVND;
    }

    public void setRateToVND(double rateToVND) {
        this.rateToVND = rateToVND;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
