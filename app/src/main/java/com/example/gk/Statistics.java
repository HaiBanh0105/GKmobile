package com.example.gk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.example.gk.Database.ExpenseDAO;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class Statistics extends BaseActivity {
    List<Expense> mListExpense;

    List<Expense> filteredList;
    private RecyclerView rcv;

    private Button btnBach;
    private  ExpenseAdapter expenseAdapter;

    private Spinner spinnerMonth;

    EditText edtYear;

    String  incomeStr, expenseStr, differenceStr;

    Button Export;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.statistics);
        setupToolbar(R.id.toolbarDashboard);

        Export = findViewById(R.id.btnExportPdf);

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

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1; //lấy tháng hiện tại
        spinnerMonth.setSelection(currentMonth);

        edtYear = findViewById(R.id.edtYear);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR); //Lấy năm hiện tại
        edtYear.setText(String.valueOf(currentYear));

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


        Context context = null;

        Export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String yearText = edtYear.getText().toString().trim();
                int monthText = spinnerMonth.getSelectedItemPosition();

                if (yearText.isEmpty()) {
                    Toast.makeText(Statistics.this, "Vui lòng nhập năm trước khi xuất báo cáo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (monthText == 0) {
                    Toast.makeText(Statistics.this, "Vui lòng chọn tháng trước khi xuất báo cáo", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Statistics.this, export_report.class);
                intent.putExtra("month", monthText);
                intent.putExtra("year", edtYear.getText().toString().trim());
                intent.putExtra("totalIncome", incomeStr);
                intent.putExtra("totalExpense", expenseStr);
                intent.putExtra("Difference", differenceStr);
                intent.putExtra("ListExpense", new ArrayList<>(filteredList));
                startActivity(intent);
            }
        });


    }


    private void filterByAll(int monthPosition, String keyword, String yearText) {
        filteredList = new ArrayList<>();

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
        loadSummaryInVND();

    }

    private void loadSummaryInVND() {
        final String yearStr = edtYear.getText().toString().trim();
        final int monthIndex = spinnerMonth.getSelectedItemPosition();

        if (yearStr.isEmpty()) return;

        final String monthStr = String.format("%02d", monthIndex);

        Executors.newSingleThreadExecutor().execute(() -> {
            ExpenseDAO expenseDAO = AppDatabase.getInstance(Statistics.this).expenseDAO();
            ExchangeDAO exchangeDAO = AppDatabase.getInstance(Statistics.this).exchangeDAO();

            List<Expense> filteredList;
            if (monthIndex == 0) {
                filteredList = expenseDAO.getExpensesByYearOnly(yearStr);
            } else {
                filteredList = expenseDAO.getExpensesByYearAndMonth(yearStr, monthStr);
            }

            double income = 0;
            double expense = 0;

            for (Expense e : filteredList) {
                ExchangeRate rate = exchangeDAO.getRateByCurrency(e.currency);
                double rateToVND = (rate != null) ? rate.rateToVND : 1.0;
                double amountVND = e.amount * rateToVND;

                if (e.isIncome) {
                    income += amountVND;
                } else {
                    expense += amountVND;
                }
            }


            final double totalIncomeVND = income;
            final double totalExpenseVND = expense;
            final double difference = totalIncomeVND - totalExpenseVND;

            runOnUiThread(() -> {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                incomeStr = formatter.format(totalIncomeVND);
                expenseStr = formatter.format(totalExpenseVND);
                differenceStr = formatter.format(difference);
            });



        });
    }




}