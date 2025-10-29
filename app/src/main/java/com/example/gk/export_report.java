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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class export_report extends AppCompatActivity {
    TextView tvMonthYear, tvTotalIncome, tvTotalExpense, tvDifference;
    Button btnExportPdf;

    TextView btnCancel;

    RecyclerView rcv;

    ArrayList<Expense> receivedList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_export_report);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvDifference = findViewById(R.id.tvDifference);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnCancel = findViewById(R.id.btnCancel);
        rcv = findViewById(R.id.rcv_expense);

        int month = getIntent().getIntExtra("month", 0);
        String year = getIntent().getStringExtra("year");
        String income = getIntent().getStringExtra("totalIncome");
        String expense = getIntent().getStringExtra("totalExpense");
        String difference = getIntent().getStringExtra("Difference");
        receivedList = (ArrayList<Expense>) getIntent().getSerializableExtra("ListExpense");

        ExpenseAdapter adapter = new ExpenseAdapter();
        adapter.setData(receivedList);


        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(adapter);


        // Hiển thị dữ liệu
        tvMonthYear.setText("Tháng " + month + " / " + year);
        tvTotalIncome.setText("Tổng thu: " + income);
        tvTotalExpense.setText("Tổng chi: " + expense);
        tvDifference.setText("Còn: " + difference);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng Activity hiện tại
            }
        });

        btnExportPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportPdf();
            }
        });

    }

    private void exportPdf() {
        String title = tvMonthYear.getText().toString();
        String income = tvTotalIncome.getText().toString();
        String expense = tvTotalExpense.getText().toString();
        String difference = tvDifference.getText().toString();
        ArrayList<Expense> receivedList = (ArrayList<Expense>) getIntent().getSerializableExtra("ListExpense");

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String currentTime = "Ngày tạo: " + sdf.format(date);

        PdfDocument pdfDocument = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;
        int rowHeight = 25;
        int startY = 80;
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

        // Tiêu đề
        canvas.drawText("BÁO CÁO TÀI CHÍNH", centerX, y, titlePaint); y += 30;
        canvas.drawText(title, centerX, y, contentPaint); y += 30;
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 20;


        // Vẽ tiêu đề cột
        int col1 = margin;
        int col2 = col1 + 150;
        int col3 = col2 + 100;
        int col4 = col3 + 80;

        canvas.drawText("Tên", col1 + 5, y, contentPaint);
        canvas.drawText("Số tiền", col2 + 5, y, contentPaint);
        canvas.drawText("Loại", col3 + 5, y, contentPaint);
        canvas.drawText("Ngày", col4 + 5, y, contentPaint);
        y += rowHeight;

        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 5;

        int itemCount = 0;
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
            itemCount++;
        }

        y += 20;
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 20;

        canvas.drawText(income, margin, y, contentPaint); y += 20;
        canvas.drawText(expense, margin, y, contentPaint); y += 20;
        canvas.drawText(difference, margin, y, contentPaint); y += 30;

        contentPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(currentTime, pageWidth - margin, y, contentPaint);

        pdfDocument.finishPage(page);

        File file = new File(getExternalFilesDir(null), "baocao_" + System.currentTimeMillis() + ".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Đã lưu PDF thành công!", Toast.LENGTH_LONG).show();
            pdfDocument.close();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu PDF", Toast.LENGTH_SHORT).show();
        }
    }


    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }



}