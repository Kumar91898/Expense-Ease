package com.kumar.expenseease.adapters;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kumar.expenseease.R;
import com.kumar.expenseease.activities.homeScreen;
import com.kumar.expenseease.dataModels.NotificationHelper;
import com.kumar.expenseease.dataModels.budgetClass;
import com.kumar.expenseease.dataModels.dataClass;

import java.util.List;

public class budgetAdapter extends RecyclerView.Adapter<budgetAdapter.ViewHolder> {

    private List<budgetClass> budgetClasses;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(budgetClass budgetClass, int position);
    }

    public budgetAdapter(List<com.kumar.expenseease.dataModels.budgetClass> budgetClasses, OnItemClickListener itemClickListener) {
        this.budgetClasses = budgetClasses;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        budgetClass budgetClass = budgetClasses.get(position);
        holder.bind(budgetClass, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return budgetClasses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView category;
        TextView totalBudget;
        TextView spentAmount;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.budgetImage);
            category = itemView.findViewById(R.id.categoryBudget);
            totalBudget = itemView.findViewById(R.id.totalBudget);
            spentAmount = itemView.findViewById(R.id.spentAmount);
            progressBar = itemView.findViewById(R.id.progressBar_budget);
        }

        public void bind(final budgetClass budgetClass, final OnItemClickListener itemClickListener) {
            Glide.with(itemView.getContext()).load(budgetClass.getImageUrl()).into(imageView);
            category.setText(budgetClass.getCategory());
            totalBudget.setText("Rs "+budgetClass.getTotalBudget());
            spentAmount.setText("Rs "+budgetClass.getSpentAmount());

            int total = (int) budgetClass.getTotalBudget();
            int current = (int) budgetClass.getSpentAmount();

            progressBar.setMax(total);
            progressBar.setProgress(current);

            if (current >= total){
                progressBar.setProgressDrawable(itemView.getResources().getDrawable(R.drawable.progress_bar_excede));
                sendBudgetExceededNotification(itemView.getContext(), budgetClass);
            } else {
                progressBar.setProgressDrawable(itemView.getResources().getDrawable(R.drawable.spent_bar));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(budgetClass, getAdapterPosition());
                }
            });

        }
        private void sendBudgetExceededNotification(Context context, budgetClass budgetClass) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle("Budget Exceeded")
                    .setContentText("You have exceeded your budget for " + budgetClass.getCategory())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true);

            Intent intent = new Intent(context, homeScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);
            builder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            }
        }
    }
}
