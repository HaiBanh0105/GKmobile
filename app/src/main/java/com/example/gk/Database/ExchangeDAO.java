package com.example.gk.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gk.ExchangeRate;
import com.example.gk.Expense;

import java.util.List;

@Dao
public interface ExchangeDAO {

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :base AND targetCurrency = :target LIMIT 1")
    ExchangeRate getRate(String base, String target);

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :base AND targetCurrency = :target ORDER BY lastUpdated DESC LIMIT 1")
    ExchangeRate getLatestRate(String base, String target);



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExchangeRate rate);
}

