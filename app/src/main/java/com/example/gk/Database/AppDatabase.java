package com.example.gk.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gk.ExchangeRate;
import com.example.gk.Expense;
import com.example.gk.MonthlyReport;

@Database(entities = {Expense.class, MonthlyReport.class, ExchangeRate.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "GK.db";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract ExpenseDAO expenseDAO();
    public abstract ReportDAO reportDAO();
    public abstract ExchangeDAO exchangeDAO();
}
