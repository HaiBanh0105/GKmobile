package com.example.gk;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gk.Database.ExchangeDAO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ExchangeRateAdapter extends RecyclerView.Adapter<ExchangeRateAdapter.ViewHolder> {
    private List<ExchangeRate> rates;
    private final Context context;
    private final ExchangeDAO dao;

    public ExchangeRateAdapter(Context context, List<ExchangeRate> rates, ExchangeDAO dao) {
        this.context = context;
        this.rates = rates;
        this.dao = dao;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCurrency, tvRate, tvLastUpdated;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCurrency = itemView.findViewById(R.id.tvCurrency);
            tvRate = itemView.findViewById(R.id.tvRate);
            tvLastUpdated = itemView.findViewById(R.id.tvLastUpdate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exchange_rate, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ExchangeRate rate = rates.get(position);
        holder.tvCurrency.setText(rate.currencyCode);
        holder.tvRate.setText(String.valueOf(rate.rateToVND));

        // Hiển thị thời gian cập nhật
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(rate.lastUpdated));
        holder.tvLastUpdated.setText("Cập nhật: " + formattedDate);

        holder.btnEdit.setOnClickListener(v -> showEditDialog(rate));
        holder.btnDelete.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                dao.delete(rate);
                rates.remove(position);
                ((Activity) context).runOnUiThread(() -> notifyItemRemoved(position));
            });
        });
    }


    private void showEditDialog(ExchangeRate rate) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(rate.rateToVND));

        new AlertDialog.Builder(context)
                .setTitle("Sửa tỷ giá: " + rate.currencyCode)
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    double newRate = Double.parseDouble(input.getText().toString());
                    rate.rateToVND = newRate;
                    rate.lastUpdated = System.currentTimeMillis();

                    Executors.newSingleThreadExecutor().execute(() -> {
                        dao.insert(rate);
                        ((Activity) context).runOnUiThread(() -> notifyDataSetChanged());
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    @Override
    public int getItemCount() {
        return rates.size();
    }
}
