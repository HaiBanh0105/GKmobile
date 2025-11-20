package com.example.gk.Repository;

import android.content.Context;
import android.util.Log;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.example.gk.ExchangeRate;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchangeRepository {
    private final ExchangeDAO exchangeDAO;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;

    public EchangeRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        exchangeDAO = db.exchangeDAO();
        firestore = FirebaseFirestore.getInstance();
        executor = Executors.newSingleThreadExecutor();
    }
    public void syncExchangeRatesFromFirestore() {
        firestore.collection("exchange_rate")
                .get()
                .addOnSuccessListener(query -> {
                    executor.execute(() -> {
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            ExchangeRate rate = doc.toObject(ExchangeRate.class);
                            if (rate != null) {
                                try {
                                    int id = Integer.parseInt(doc.getId());
                                    rate.setId(id);
                                } catch (NumberFormatException e) {
                                    Log.e("Firestore", "ID không hợp lệ: " + doc.getId());
                                    continue;
                                }

                                // Kiểm tra trùng lặp trước khi insert
                                ExchangeRate existing = exchangeDAO.getExchangeById(rate.getId());
                                if (existing == null) {
                                    exchangeDAO.insert(rate);
                                    Log.d("Sync", "Đã thêm ExchangeRate mới từ Firestore: " + rate.getId());
                                } else {
                                    Log.d("Sync", "Bỏ qua ExchangeRate đã tồn tại: " + rate.getId());
                                }
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Lỗi khi đồng bộ ExchangeRate từ Firestore", e));
    }



}
