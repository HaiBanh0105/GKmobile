package com.example.gk;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
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
import java.util.concurrent.Executors;

public class Export_report extends AppCompatActivity {

    // UI components
    private TextView tvMonthYear, tvTotalIncome, tvTotalExpense, tvDifference, btnCancel;
    private Button btnExportPdf;
    private RecyclerView rcv;

    // Data
    private ArrayList<Expense> receivedList;
    private int month;
    private String year;

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
        String income = getIntent().getStringExtra("totalIncome");
        String expense = getIntent().getStringExtra("totalExpense");
        String difference = getIntent().getStringExtra("Difference");
        receivedList = (ArrayList<Expense>) getIntent().getSerializableExtra("ListExpense");

        tvMonthYear.setTag("Tháng " + month + " / " + year);
        tvTotalIncome.setTag(income);
        tvTotalExpense.setTag(expense);
        tvDifference.setTag(difference);
    }

    private void setupRecyclerView() {
        ExpenseAdapter adapter = new ExpenseAdapter();
        adapter.setData(receivedList);
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(adapter);
    }

    private void displayReportInfo() {
        tvMonthYear.setText((String) tvMonthYear.getTag());
        tvTotalIncome.setText("Tổng thu: " + tvTotalIncome.getTag() + " VND");
        tvTotalExpense.setText("Tổng chi: " + tvTotalExpense.getTag() + " VND");
        tvDifference.setText("Chênh lệch: " + tvDifference.getTag() + " VND");
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());
        btnExportPdf.setOnClickListener(v -> exportPdf());
    }

    private void exportPdf() {
        String title = tvMonthYear.getText().toString();
        String income = (String) tvTotalIncome.getTag();
        String expense = (String) tvTotalExpense.getTag();
        String difference = (String) tvDifference.getTag();

        Date date = new Date();
        String currentTime = "Ngày tạo: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(date);

        PdfDocument pdfDocument = new PdfDocument();
        int pageWidth = 595, pageHeight = 842, margin = 40, rowHeight = 25, startY = 80;
        int y = startY;

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

        // Column titles
        int col1 = margin, col2 = col1 + 150, col3 = col2 + 100, col4 = col3 + 80;
        canvas.drawText("Tên", col1 + 5, y, contentPaint);
        canvas.drawText("Số tiền", col2 + 5, y, contentPaint);
        canvas.drawText("Loại", col3 + 5, y, contentPaint);
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

            canvas.drawRect(col1, y, col2, y + rowHeight, borderPaint);
            canvas.drawRect(col2, y, col3, y + rowHeight, borderPaint);
            canvas.drawRect(col3, y, col4, y + rowHeight, borderPaint);
            canvas.drawRect(col4, y, pageWidth - margin, y + rowHeight, borderPaint);

            canvas.drawText(e.title, col1 + 5, y + 17, contentPaint);
            canvas.drawText(e.amount + " " + e.currency, col2 + 5, y + 17, contentPaint);
            canvas.drawText(e.isIncome ? "Thu" : "Chi", col3 + 5, y + 17, contentPaint);
            canvas.drawText(formatDate(e.timestamp), col4 + 5, y + 17, contentPaint);

            y += rowHeight;
        }

        // Summary
        y += 20;
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 20;
        canvas.drawText("Tổng thu: " + income + " VND", margin, y, contentPaint); y += 20;
        canvas.drawText("Tổng chi: " + expense + " VND", margin, y, contentPaint); y += 20;
        canvas.drawText("Chênh lệch: " + difference + " VND", margin, y, contentPaint); y += 30;

        contentPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(currentTime, pageWidth - margin, y, contentPaint);

        pdfDocument.finishPage(page);

        // Save PDF
        File file = new File(getExternalFilesDir(null), "baocao_" + System.currentTimeMillis() + ".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Đã lưu PDF thành công!", Toast.LENGTH_LONG).show();

            // Save report to database
            MonthlyReport report = new MonthlyReport();
            report.setMonth(month);
            report.setYear(Integer.parseInt(year));
            report.setTotalIncome(Double.parseDouble(income));
            report.setTotalExpense(Double.parseDouble(expense));
            report.setGeneratedAt(System.currentTimeMillis());

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                db.reportDAO().insertExpense(report);
            });

            pdfDocument.close();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(long timestamp) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(timestamp));
    }
}
