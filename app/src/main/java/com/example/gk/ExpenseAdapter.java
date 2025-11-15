package com.example.gk;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gk.Database.AppDatabase;
import com.example.gk.Database.ExchangeDAO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>{
    List<Expense> mlistExpense;

    public void setData(List<Expense> list){
        this.mlistExpense = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense,parent,false);
        return new ExpenseViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = mlistExpense.get(position);
        if (expense == null) return;

        // Tiêu đề
        holder.tvTitle.setText(expense.getTitle());

        // Format số tiền: có dấu phẩy và đơn vị VND
        String formattedAmount = formatCurrency(holder.itemView, expense.getAmount());

        holder.tvAmount.setText(formattedAmount);

        holder.tvCurrency.setVisibility(View.GONE);

        // Format danh mục: viết hoa chữ cái đầu
        String category = expense.getCategory();
        if (category != null && !category.isEmpty()) {
            category = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
        } else {
            category = "Khác";
        }
        holder.tvCategory.setText(category);

        // Định dạng thời gian
        String formattedDate = formatDate(expense.getTimestamp());
        holder.tvDate.setText(formattedDate);
    }


    @Override
    public int getItemCount() {
        if(mlistExpense != null){
            return mlistExpense.size();
        }
        return 0;
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder{

        private TextView tvTitle;
        private TextView tvAmount;
        private TextView tvCategory;

        private TextView tvCurrency;

        private TextView tvDate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCurrency = itemView.findViewById(R.id.tvCurrency);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    private String formatCurrency(View view, double amountVND) {
        ExchangeDAO exchangeDAO = AppDatabase.getInstance(view.getContext()).exchangeDAO();
        ExchangeRate rate = exchangeDAO.getRate(AppConstants.currentCurrency, "VND");
        double rateFromVND = (rate != null && rate.rate > 0) ? 1.0 / rate.rate : 1.0;

        double convertedAmount = amountVND * rateFromVND;
        return String.format("%,.0f %s", convertedAmount, AppConstants.currentCurrency);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }



}
