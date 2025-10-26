package com.example.gk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ExchangeRateDialogFragment extends DialogFragment {
    private ExchangeDAO dao;
    private RecyclerView recyclerView;
    private ExchangeRateAdapter adapter;
    private List<ExchangeRate> rateList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_exchange_rate, container, false);

        dao = AppDatabase.getInstance(requireContext()).exchangeDAO();
        recyclerView = view.findViewById(R.id.recyclerRates);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ExchangeRateAdapter(getContext(), rateList, dao);
        recyclerView.setAdapter(adapter);

        MaterialButton btnClose = view.findViewById(R.id.btnClose);
        Button btnAdd = view.findViewById(R.id.btnAddRate);
        btnAdd.setOnClickListener(v -> showAddDialog());
        btnClose.setOnClickListener(v -> dismiss());

        loadRates();

        return view;
    }

    private void loadRates() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ExchangeRate> rates = dao.getListExchangeRates();
            requireActivity().runOnUiThread(() -> {
                rateList.clear();
                rateList.addAll(rates);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_exchange_rate, null);

        EditText edtCode = dialogView.findViewById(R.id.edtCurrencyCode);
        EditText edtRate = dialogView.findViewById(R.id.edtExchangeRate);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Thêm tỷ giá mới")
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                String code = edtCode.getText().toString().trim();
                String rateStr = edtRate.getText().toString().trim();

                if (code.isEmpty() || rateStr.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double rate = Double.parseDouble(rateStr);
                    ExchangeRate newRate = new ExchangeRate(code, rate, System.currentTimeMillis());

                    Executors.newSingleThreadExecutor().execute(() -> {
                        dao.insert(newRate);
                        requireActivity().runOnUiThread(() -> {
                            loadRates();
                            dialog.dismiss();
                        });
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Tỷ giá không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
