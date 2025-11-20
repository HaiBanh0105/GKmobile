package com.example.gk.Repository;

import android.content.Context;
import android.util.Log;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExpenseDAO;
import com.example.gk.Expense;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {
    private final ExpenseDAO expenseDAO;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;

    public ExpenseRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        expenseDAO = db.expenseDAO();
        firestore = FirebaseFirestore.getInstance();
        executor = Executors.newSingleThreadExecutor();
    }

    // Lưu vào Room và Firestore với ID đồng bộ

    public void syncFromFirestore() {
        firestore.collection("expenses")
                .get()
                .addOnSuccessListener(query -> {
                    executor.execute(() -> {
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            Expense expense = doc.toObject(Expense.class);
                            if (expense != null) {
                                try {
                                    int id = Integer.parseInt(doc.getId());
                                    expense.setId(id);
                                } catch (NumberFormatException e) {
                                    Log.e("Firestore", "ID không hợp lệ: " + doc.getId());
                                    continue;
                                }

                                // Kiểm tra trùng lặp trước khi insert
                                Expense existing = expenseDAO.getExpenseById(expense.getId());
                                if (existing == null) {
                                    expenseDAO.insertExpense(expense);
                                    Log.d("Sync", "Đã thêm Expense mới từ Firestore: " + expense.getId());
                                } else {
                                    Log.d("Sync", "Bỏ qua Expense đã tồn tại: " + expense.getId());
                                }
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Lỗi khi đồng bộ từ Firestore", e));
    }

}
