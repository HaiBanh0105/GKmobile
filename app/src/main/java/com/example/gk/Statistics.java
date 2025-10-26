package com.example.gk;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

public class Statistics extends BaseActivity {
    List<Expense> mListExpense;
    private RecyclerView rcv;

    private Button btnBach;
    private  ExpenseAdapter expenseAdapter;

    private Spinner spinnerMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.statistics);
        setupToolbar(R.id.toolbarDashboard);

        spinnerMonth = findViewById(R.id.spnMonthFilter);
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

        String[] months = {"Không chọn", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6"
                , "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11","Tháng 12"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

    }
}