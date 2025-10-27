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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class export_report extends AppCompatActivity {
    TextView tvMonthYear, tvTotalIncome, tvTotalExpense, tvDifference;
    Button btnExportPdf;

    TextView btnCancel;



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

        // Nhận dữ liệu từ Dashboard
        int month = getIntent().getIntExtra("month", 0);
        String year = getIntent().getStringExtra("year");
        String income = getIntent().getStringExtra("totalIncome");
        String expense = getIntent().getStringExtra("totalExpense");
        String difference = getIntent().getStringExtra("Difference");

        // Hiển thị dữ liệu
        tvMonthYear.setText("Tháng " + month + " / " + year);
        tvTotalIncome.setText("Tổng " + income);
        tvTotalExpense.setText("Tổng " + expense);
        tvDifference.setText(difference);

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

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String currentTime = "Ngày tạo: " + sdf.format(date);

        PdfDocument pdfDocument = new PdfDocument();

        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Paint cho tiêu đề
        Paint titlePaint = new Paint();
        titlePaint.setTextSize(20f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(Color.BLACK);

        // Paint cho nội dung
        Paint contentPaint = new Paint();
        contentPaint.setTextSize(16f);
        contentPaint.setColor(Color.DKGRAY);

        // Paint cho dòng phân cách
        Paint linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(1f);

        int centerX = pageWidth / 2;
        int y = 80;

        // Vẽ tiêu đề
        canvas.drawText("BÁO CÁO TÀI CHÍNH", centerX, y, titlePaint); y += 40;
        canvas.drawText(title, centerX, y, contentPaint); y += 30;

        // Dòng phân cách
        canvas.drawLine(50, y, pageWidth - 50, y, linePaint); y += 30;

        // Vẽ nội dung
        canvas.drawText(income, 60, y, contentPaint); y += 30;
        canvas.drawText(expense, 60, y, contentPaint); y += 30;
        canvas.drawText(difference, 60, y, contentPaint); y += 30;

        // Dòng phân cách
        canvas.drawLine(50, y, pageWidth - 50, y, linePaint); y += 30;

        // Vẽ thời gian tạo
        canvas.drawText(currentTime, 60, y, contentPaint); y += 30;

        pdfDocument.finishPage(page);

        // Lưu file
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


}