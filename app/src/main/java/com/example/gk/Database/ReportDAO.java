package com.example.gk.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import com.example.gk.MonthlyReport;

import java.util.List;

@Dao
public interface ReportDAO {

    @Insert
    void insertExpense(MonthlyReport report);

    @Query("select * from monthly_reports")
    List<MonthlyReport> getListReport();



}
