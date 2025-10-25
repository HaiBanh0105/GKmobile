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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExchangeRate rate);

    @Delete
    void delete(ExchangeRate rate);

    @Query("SELECT * FROM exchange_rates")
    List<ExchangeRate> getListExchangeRates();

    @Query("SELECT currencyCode FROM exchange_rates")
    List<String> getAllCurrencyCodes();

    @Query("SELECT * FROM exchange_rates WHERE currencyCode = :code LIMIT 1")
    ExchangeRate getRateByCurrency(String code);

}
