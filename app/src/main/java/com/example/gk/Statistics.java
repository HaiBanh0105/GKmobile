package com.example.gk;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gk.Database.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class Statistics extends AppCompatActivity {
    List<Expense> mListExpense;
    private RecyclerView rcv;

    private Button btnBach;
    private  ExpenseAdapter expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.statistics);

        rcv = findViewById(R.id.rcv_expense);
        btnBach = findViewById(R.id.btnBack);


        expenseAdapter = new ExpenseAdapter();
        mListExpense = new ArrayList<>();
        expenseAdapter.setData(mListExpense);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcv.setLayoutManager(linearLayoutManager);
        rcv.setAdapter(expenseAdapter);

        mListExpense = AppDatabase.getInstance(this).expenseDAO().getListExpense();
        expenseAdapter.setData(mListExpense);

        btnBach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}