package com.example.gk.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gk.CurrencyInfo;
import com.example.gk.ExchangeRate;
import com.example.gk.Expense;
import com.example.gk.MonthlyReport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Expense.class, MonthlyReport.class, ExchangeRate.class, CurrencyInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "GK.db";
    private static AppDatabase instance;

    // Khai báo Executor để chạy background
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract ExpenseDAO expenseDAO();
    public abstract ReportDAO reportDAO();
    public abstract ExchangeDAO exchangeDAO();
    public abstract CurrencyDAO currencyDAO();

}
