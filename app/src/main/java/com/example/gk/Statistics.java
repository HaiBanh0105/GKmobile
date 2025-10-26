package com.example.gk;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Calendar;
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

        EditText edtYear = findViewById(R.id.edtYear);
        edtYear.setText("2025");
        EditText edtSearch = findViewById(R.id.edtSearch);
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String keyword = edtSearch.getText().toString();
                String yearText = edtYear.getText().toString();
                filterByAll(position, keyword, yearText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int monthPosition = spinnerMonth.getSelectedItemPosition();
                String yearText = edtYear.getText().toString();
                filterByAll(monthPosition, s.toString(), yearText);
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });



        edtYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int monthPosition = spinnerMonth.getSelectedItemPosition();
                String keyword = edtSearch.getText().toString();
                String yearText = s.toString();
                filterByAll(monthPosition, keyword, yearText);
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });


    }


    private void filterByAll(int monthPosition, String keyword, String yearText) {
        List<Expense> filteredList = new ArrayList<>();

        for (Expense expense : mListExpense) {
            boolean matchMonth = (monthPosition == 0); // 0 = "Không chọn"
            boolean matchKeyword = keyword.isEmpty();
            boolean matchYear = yearText.isEmpty();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(expense.getTimestamp());
            int expenseMonth = calendar.get(Calendar.MONTH) + 1;
            int expenseYear = calendar.get(Calendar.YEAR);

            // Kiểm tra tháng
            if (!matchMonth) {
                matchMonth = (expenseMonth == monthPosition);
            }

            // Kiểm tra từ khóa
            if (!matchKeyword) {
                String title = expense.getTitle().toLowerCase();
                String category = expense.getCategory().toLowerCase();
                matchKeyword = title.contains(keyword.toLowerCase()) || category.contains(keyword.toLowerCase());
            }

            // Kiểm tra năm
            if (!matchYear) {
                try {
                    int inputYear = Integer.parseInt(yearText);
                    matchYear = (expenseYear == inputYear);
                } catch (NumberFormatException e) {
                    matchYear = true; // Nếu nhập sai, bỏ qua lọc theo năm
                }
            }

            if (matchMonth && matchKeyword && matchYear) {
                filteredList.add(expense);
            }
        }

        expenseAdapter.setData(filteredList);
    }




}