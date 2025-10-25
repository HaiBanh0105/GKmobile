package com.example.gk.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.gk.Expense;

import java.util.List;

@Dao
public interface ExpenseDAO {
    @Insert
    void insertExpense(Expense expense);

    @Query("select * from expenses")
    List<Expense> getListExpense();

//    @Query("SELECT * FROM expenses WHERE strftime('%m', datetime(timestamp / 1000, 'unixepoch')) = :month AND strftime('%Y', datetime(timestamp / 1000, 'unixepoch')) = :year")
//    List<Expense> getExpensesByMonth(String month, String year);

//    @Query("SELECT * FROM exchange_rates")
//    List<ExchangeRate> getAllExchangeRates();
}
