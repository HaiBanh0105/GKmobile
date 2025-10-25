package com.example.gk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class MonthlyReportAdapter extends RecyclerView.Adapter<MonthlyReportAdapter.MonthlyReportViewHolder> {
    private List<MonthlyReport> mlistReport;

    public void setData(List<MonthlyReport> list){
        this.mlistReport = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MonthlyReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense,parent,false);
        return new MonthlyReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthlyReportViewHolder holder, int position) {
        MonthlyReport report = mlistReport.get(position);
        if(report == null){
            return;
        }
        DecimalFormat formatter = new DecimalFormat("#,###.##");
        holder.tvTotalIncome.setText(formatter.format(report.getTotalIncome()) + " đ");
        holder.tvTotalExpense.setText(formatter.format(report.getTotalExpense()) + " đ");


    }

    @Override
    public int getItemCount() {
        if(mlistReport != null){
            return mlistReport.size();
        }
        return 0;
    }

    public class MonthlyReportViewHolder extends RecyclerView.ViewHolder{

        private TextView tvTotalIncome;
        private TextView tvTotalExpense;


        public MonthlyReportViewHolder(@NonNull View itemView) {
            super(itemView);
//            tvTotalIncome = itemView.findViewById(R.id.totalIncome);
//            tvTotalExpense = itemView.findViewById(R.id.totalExpense);
        }
    }
}
