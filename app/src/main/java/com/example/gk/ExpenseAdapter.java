package com.example.gk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        holder.tvTitle.setText(expense.getTitle());
        holder.tvAmount.setText(String.valueOf(expense.getAmount()));
        holder.tvCurrency.setText(expense.getCurrency());
        holder.tvCategory.setText(expense.getCategory());
        holder.tvType.setText(expense.isIncome() ? "Thu" : "Chi");

        // Định dạng thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(expense.getTimestamp()));
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

        private TextView tvType;
        private TextView tvDate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCurrency = itemView.findViewById(R.id.tvCurency);
            tvType = itemView.findViewById(R.id.tvType);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }





}
