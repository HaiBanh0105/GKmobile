package com.example.gk.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gk.CurrencyInfo;

import java.util.List;

@Dao
public interface CurrencyDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CurrencyInfo currency);

    @Query("SELECT * FROM currencies")
    List<CurrencyInfo> getAllCurrencies();

    @Query("SELECT name FROM currencies WHERE code = :code LIMIT 1")
    String getNameByCode(String code);
}

