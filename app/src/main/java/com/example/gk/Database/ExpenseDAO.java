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

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC LIMIT 1")
    Expense getLatestExpense();

    @Query("SELECT MAX(id) FROM expenses")
    int getMaxExpenseId();

    @Query("SELECT * FROM expenses WHERE id = :id")
    Expense getExpenseById(int id);


    @Query("SELECT * FROM expenses WHERE strftime('%Y', datetime(timestamp / 1000, 'unixepoch')) = :year AND strftime('%m', datetime(timestamp / 1000, 'unixepoch')) = :monthStr")
    List<Expense> getExpensesByYearAndMonth(String year, String monthStr);

    @Query("SELECT * FROM expenses WHERE strftime('%Y', datetime(timestamp / 1000, 'unixepoch')) = :year")
    List<Expense> getExpensesByYearOnly(String year);

    @Query("SELECT SUM(amount) FROM expenses " +
            "WHERE isIncome = 1 AND strftime('%Y', datetime(timestamp / 1000, 'unixepoch')) = :year " +
            "AND strftime('%m', datetime(timestamp / 1000, 'unixepoch')) = :monthStr")
    Double getTotalIncome(String year, String monthStr);

    @Query("SELECT SUM(amount) FROM expenses " +
            "WHERE isIncome = 0 AND strftime('%Y', datetime(timestamp / 1000, 'unixepoch')) = :year " +
            "AND strftime('%m', datetime(timestamp / 1000, 'unixepoch')) = :monthStr")
    Double getTotalExpense(String year, String monthStr);





//    @Query("SELECT * FROM expenses WHERE strftime('%m', datetime(timestamp / 1000, 'unixepoch')) = :month AND strftime('%Y', datetime(timestamp / 1000, 'unixepoch')) = :year")
//    List<Expense> getExpensesByMonth(String month, String year);

//    @Query("SELECT * FROM exchange_rates")
//    List<ExchangeRate> getAllExchangeRates();
}
