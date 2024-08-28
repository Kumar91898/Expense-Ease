package com.kumar.expenseease.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kumar.expenseease.R;
import com.kumar.expenseease.dataModels.dataClass;

import java.util.List;

public class incomeTransactionsAdapter extends RecyclerView.Adapter<incomeTransactionsAdapter.ViewHolder> {

    private List<dataClass> dataClasses;
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(dataClass dataClass, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(dataClass dataClass, int position);
    }

    public incomeTransactionsAdapter(List<dataClass> dataClasses, OnItemClickListener itemClickListener, OnItemLongClickListener itemLongClickListener) {
        this.dataClasses = dataClasses;
        this.itemClickListener = itemClickListener;
        this.itemLongClickListener = itemLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transactions_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        dataClass dataClass = dataClasses.get(position);
        holder.bind(dataClass, itemClickListener, itemLongClickListener);
    }

    @Override
    public int getItemCount() {
        return dataClasses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title;
        TextView date;
        TextView amount;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            amount = itemView.findViewById(R.id.amount);
        }

        public void bind(final dataClass dataClass, final OnItemClickListener itemClickListener, final OnItemLongClickListener itemLongClickListener) {
            Glide.with(itemView.getContext()).load(dataClass.getImageUrl()).into(imageView);
            title.setText(dataClass.getTitle());
            date.setText(dataClass.getDate());

            if (dataClass.isIncome()) {
                amount.setTextColor(itemView.getContext().getResources().getColor(R.color.green));
                amount.setText("+" + dataClass.getAmount());
            } else {
                amount.setTextColor(itemView.getContext().getResources().getColor(R.color.red));
                amount.setText("-" + dataClass.getAmount());
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(dataClass, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemLongClickListener.onItemLongClick(dataClass, getAdapterPosition());
                    return true;
                }
            });
        }
    }
}
