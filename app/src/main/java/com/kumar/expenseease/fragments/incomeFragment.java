package com.kumar.expenseease.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kumar.expenseease.R;
import com.kumar.expenseease.adapters.incomeTransactionsAdapter;
import com.kumar.expenseease.adapters.transactionsAdapter;
import com.kumar.expenseease.dataModels.dataClass;
import com.kumar.expenseease.databinding.FragmentIncomeBinding;

import java.util.ArrayList;
import java.util.List;

public class incomeFragment extends Fragment {

    FragmentIncomeBinding binding;
    private List<dataClass> dataClass;
    private incomeTransactionsAdapter transactionsAdapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String email;
    private Dialog loadingDialog, dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentIncomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        email = currentUser.getEmail();

        openLoadingDialogue();

        // Initialize the RecyclerView with a LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewIncome.setLayoutManager(layoutManager);

        // Initialize the dataClass list and the transactionsAdapter
        dataClass = new ArrayList<>();
        transactionsAdapter = new incomeTransactionsAdapter(dataClass, new incomeTransactionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.kumar.expenseease.dataModels.dataClass dataClass, int position) {
                openDialog(dataClass);
            }
        }, new incomeTransactionsAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(com.kumar.expenseease.dataModels.dataClass dataClass, int position) {

            }
        });

        fetchIncomeTransactions();

        // Set the adapter to the RecyclerView
        binding.recyclerViewIncome.setAdapter(transactionsAdapter);
    }

    private void fetchIncomeTransactions() {
        // Fetch income transactions
        db.collection("income")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("imageUrl");
                            String title = document.getString("incomeName");
                            String date = document.getString("incomeDate");
                            String documentId = document.getId();
                            double amount = document.getDouble("incomeAmount");
                            dataClass.add(new dataClass(title, date, imageUrl, "Income", documentId, amount, true));
                        }

                        transactionsAdapter.notifyDataSetChanged();
                        updateUI();

                    } else {
                        Toast.makeText(getContext(), "Failed to load income transactions!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openLoadingDialogue(){
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_dialogue_box);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    private void openDialog(dataClass dataClass) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.info_dialogue_box);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        dialog.setCancelable(true);
        dialog.show();

        ImageView imageView = dialog.findViewById(R.id.image_info);
        TextView category = dialog.findViewById(R.id.category_info);
        TextView name = dialog.findViewById(R.id.name_info);
        TextView date = dialog.findViewById(R.id.date_info);
        TextView amount = dialog.findViewById(R.id.amount_info);

        Glide.with(getContext()).load(dataClass.getImageUrl()).into(imageView);
        name.setText(dataClass.getTitle());
        category.setText(dataClass.getCategory());
        date.setText(dataClass.getDate());
        amount.setText("Rs "+dataClass.getAmount());
    }

    private void updateUI() {
        if (dataClass.isEmpty()) {
            binding.recyclerViewIncome.setVisibility(View.GONE);
            binding.emptyLayoutIncome.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewIncome.setVisibility(View.VISIBLE);
            binding.emptyLayoutIncome.setVisibility(View.GONE);
        }
    }
}