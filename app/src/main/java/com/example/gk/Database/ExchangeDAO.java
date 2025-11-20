package com.example.gk.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gk.ExchangeRate;

import java.util.List;

@Dao
public interface ExchangeDAO {

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :base AND targetCurrency = :target LIMIT 1")
    ExchangeRate getRate(String base, String target);

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :base AND targetCurrency = :target ORDER BY lastUpdated DESC LIMIT 1")
    ExchangeRate getLatestRate(String base, String target);

    @Query("SELECT DISTINCT baseCurrency FROM exchange_rates")
    List<String> getAllBaseCurrencies();

    @Query("SELECT * FROM exchange_rates ORDER BY id DESC LIMIT 1")
    ExchangeRate getLatestExchange();

    @Query("SELECT * FROM exchange_rates WHERE id = :id LIMIT 1")
    ExchangeRate getExchangeById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExchangeRate rate);
}

