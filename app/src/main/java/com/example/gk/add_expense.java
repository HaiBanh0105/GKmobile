package com.example.gk;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gk.Database.AppDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class add_expense extends AppCompatActivity {

    private EditText edttitle;
    private EditText edtamount;
    private Spinner spinnercategory;
    private Spinner spinnercurrency;
    private String selectedCategory;
    private String selectedCurrency;

    private long timestamp;
    private RadioGroup radioGroup;


    private Button btnAdd;

    private RadioButton radioIncome;
    private RadioButton radioExpense;
    private boolean isIncome;
    private RecyclerView rcv;

    private  ExpenseAdapter expenseAdapter;
    List<Expense> mListExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_expense);

        initUi();

        Button btnManageCurrency = findViewById(R.id.btnManageCurrency);

        loadCurrencyList();

        btnManageCurrency.setOnClickListener(v -> {
            Intent intent = new Intent(add_expense.this, ExchangeRateActivity.class);
            startActivity(intent);
        });

        List<String> categories = Arrays.asList("Ăn uống", "Giải trí", "Lương", "Hóa đơn");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );
        spinnercategory.setAdapter(adapter);



        // Lấy lựa chọn hiện tại
        String selectedCategory = "";
        if (spinnercategory.getSelectedItem() != null) {
            selectedCategory = spinnercategory.getSelectedItem().toString();
        }

        String selectedCurrency = "";
        if (spinnercurrency.getSelectedItem() != null) {
            selectedCurrency = spinnercurrency.getSelectedItem().toString();
        }

        timestamp = System.currentTimeMillis();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpense();
            }
        });

    };

    private void addExpense() {
        String strTitle = edttitle.getText().toString().trim();
        String strAmount = edtamount.getText().toString().trim();

        if (TextUtils.isEmpty(strTitle) || TextUtils.isEmpty(strAmount)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double dbAmount;
        try {
            dbAmount = Double.parseDouble(strAmount);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = radioGroup.getCheckedRadioButtonId();
        isIncome = (selectedId == R.id.radioIncome);
        String selectedCategory = "";
        if (spinnercategory.getSelectedItem() != null) {
            selectedCategory = spinnercategory.getSelectedItem().toString();
        }

        String selectedCurrency = "";
        if (spinnercurrency.getSelectedItem() != null) {
            selectedCurrency = spinnercurrency.getSelectedItem().toString();
        }

        long timestamp = System.currentTimeMillis();

        Expense expense = new Expense(strTitle, dbAmount, selectedCurrency, selectedCategory, isIncome, timestamp);
        AppDatabase.getInstance(this).expenseDAO().insertExpense(expense);
        Toast.makeText(this, "Thêm expense thành công", Toast.LENGTH_SHORT).show();
        edttitle.setText("");
        edtamount.setText("");
        radioGroup.clearCheck();


        List<Expense> list = AppDatabase.getInstance(this).expenseDAO().getListExpense();
        for (Expense e : list) {
            Log.d("Expense", e.getTitle() + " - " + e.getAmount());
        }

    }
    private void initUi(){
        edttitle = findViewById(R.id.addTitle);
        edtamount = findViewById(R.id.addAmount);
        spinnercategory = findViewById(R.id.category);
        spinnercurrency = findViewById(R.id.currency);

        btnAdd = findViewById(R.id.btnAdd);
        radioGroup = findViewById(R.id.radioGroupType);
        radioIncome = findViewById(R.id.radioIncome);
        radioExpense = findViewById(R.id.radioExpense);
        rcv = findViewById(R.id.rcv_expense);
    }

    private void loadCurrencyList() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> currencyList = AppDatabase.getInstance(getApplicationContext())
                    .exchangeDAO()
                    .getAllCurrencyCodes();

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, currencyList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnercurrency.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrencyList(); // Tự động reload khi quay lại từ màn hình quản lý
    }
}