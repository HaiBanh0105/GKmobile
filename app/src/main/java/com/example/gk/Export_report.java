package com.example.gk;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gk.Database.AppDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Export_report extends AppCompatActivity {

    private TextView tvMonthYear, tvTotalIncome, tvTotalExpense, tvDifference, btnCancel;
    private Button btnExportPdf;
    private RecyclerView rcv;

    private ArrayList<Expense> receivedList;
    private int month;
    private String year;
    private String incomeStr, expenseStr, differenceStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_export_report);

        initViews();
        loadIntentData();
        setupRecyclerView();
        displayReportInfo();
        setupListeners();
    }

    private void initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvDifference = findViewById(R.id.tvDifference);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnCancel = findViewById(R.id.btnCancel);
        rcv = findViewById(R.id.rcv_expense);
    }

    private void loadIntentData() {
        month = getIntent().getIntExtra("month", 0);
        year = getIntent().getStringExtra("year");
        incomeStr = getIntent().getStringExtra("totalIncome");
        expenseStr = getIntent().getStringExtra("totalExpense");
        differenceStr = getIntent().getStringExtra("Difference");
        receivedList = (ArrayList<Expense>) getIntent().getSerializableExtra("ListExpense");

        tvMonthYear.setTag("Tháng " + month + " / " + year);
        tvTotalIncome.setTag(incomeStr);
        tvTotalExpense.setTag(expenseStr);
        tvDifference.setTag(differenceStr);
    }

    private void setupRecyclerView() {
        ExpenseAdapter adapter = new ExpenseAdapter();
        adapter.setData(receivedList);
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(adapter);
    }

    private void displayReportInfo() {
        tvMonthYear.setText((String) tvMonthYear.getTag());
        tvTotalIncome.setText("Tổng thu: " + incomeStr + " " + AppConstants.currentCurrency);
        tvTotalExpense.setText("Tổng chi: " + expenseStr + " " + AppConstants.currentCurrency);
        tvDifference.setText(differenceStr + " " + AppConstants.currentCurrency);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());
        btnExportPdf.setOnClickListener(v -> exportPdf());
    }

    private void exportPdf() {
        String title = tvMonthYear.getText().toString();
        String currentTime = "Ngày tạo: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        double rate = getRateFromVND(AppConstants.currentCurrency);

        PdfDocument pdfDocument = new PdfDocument();
        int pageWidth = 595, pageHeight = 842, margin = 40, rowHeight = 30, startY = 80;
        int y = startY;

        // Paints
        Paint titlePaint = new Paint();
        titlePaint.setTextSize(20f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(Color.BLACK);

        Paint contentPaint = new Paint();
        contentPaint.setTextSize(14f);
        contentPaint.setColor(Color.BLACK);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(1f);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        int centerX = pageWidth / 2;

        // Header
        canvas.drawText("BÁO CÁO THU CHI", centerX, y, titlePaint); y += 30;
        canvas.drawText(title, centerX, y, contentPaint); y += 30;
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 20;

        // Column setup (4 cột: Mô tả – Số tiền – Danh mục – Ngày)
        int tableWidth = pageWidth - 2 * margin;
        int col1Width = (int)(tableWidth * 0.30); // Mô tả
        int col2Width = (int)(tableWidth * 0.25); // Số tiền
        int col3Width = (int)(tableWidth * 0.25); // Danh mục
        int col4Width = (int)(tableWidth * 0.20); // Ngày

        int col1 = margin;
        int col2 = col1 + col1Width;
        int col3 = col2 + col2Width;
        int col4 = col3 + col3Width;

        // Column titles
        contentPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Mô tả", col1 + 5, y, contentPaint);
        canvas.drawText("Số tiền", col2 + 5, y, contentPaint);
        canvas.drawText("Danh mục", col3 + 5, y, contentPaint);
        canvas.drawText("Ngày", col4 + 5, y, contentPaint);
        y += rowHeight;
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 5;

        // Rows
        for (Expense e : receivedList) {
            if (y + rowHeight > pageHeight - 100) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = startY;
            }

            // Draw cell borders
            canvas.drawRect(col1, y, col2, y + rowHeight, borderPaint);
            canvas.drawRect(col2, y, col3, y + rowHeight, borderPaint);
            canvas.drawRect(col3, y, col4, y + rowHeight, borderPaint);
            canvas.drawRect(col4, y, pageWidth - margin, y + rowHeight, borderPaint);

            // Draw cell content
            contentPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(e.title, col1 + 5, y + 20, contentPaint);

            double convertedAmount = e.amount / rate;
            String formattedAmount = String.format("%,.0f %s", convertedAmount, AppConstants.currentCurrency);
            canvas.drawText(formattedAmount, col2 + 5, y + 20, contentPaint);

            String category = e.category != null ? e.category : "Khác";
            category = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
            canvas.drawText(category, col3 + 5, y + 20, contentPaint);

            canvas.drawText(formatDate(e.timestamp), col4 + 5, y + 20, contentPaint);

            y += rowHeight;
        }

        // Summary
        y += 20;
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 20;

        contentPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Tổng thu: " + incomeStr + " " + AppConstants.currentCurrency, margin, y, contentPaint); y += 20;
        canvas.drawText("Tổng chi: " + expenseStr + " " + AppConstants.currentCurrency, margin, y, contentPaint); y += 20;
        canvas.drawText("Chênh lệch: " + differenceStr + " " + AppConstants.currentCurrency, margin, y, contentPaint); y += 30;

        contentPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(currentTime, pageWidth - margin, y, contentPaint);

        pdfDocument.finishPage(page);

        // Save PDF

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "baocao_" + System.currentTimeMillis() + ".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();
            Toast.makeText(this, "Đã xuất file PDF thành công!!, file nằm ở thư mục download của máy", Toast.LENGTH_LONG).show();

            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu PDF", Toast.LENGTH_SHORT).show();
        }
    }





    private String formatDate(long timestamp) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(timestamp));
    }

    private double getRateFromVND(String targetCurrency) {
        ExchangeRate rate = AppDatabase.getInstance(this).exchangeDAO().getRate(targetCurrency,"VND");
        return (rate != null && rate.rate > 0) ? rate.rate : 1.0;
    }

}
