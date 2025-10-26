package com.example.gk;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "monthly_reports")
public class MonthlyReport {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int month;              // Tháng (1–12)
    public int year;               // Năm
    public double totalIncome;     // Tổng thu nhập
    public double totalExpense;    // Tổng chi tiêu
    public long generatedAt;       // Thời điểm tạo báo cáo

    public MonthlyReport(){

    }
    public long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
