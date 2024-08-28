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
import com.kumar.expenseease.adapters.expenseTransactionsAdapter;
import com.kumar.expenseease.adapters.transactionsAdapter;
import com.kumar.expenseease.dataModels.dataClass;
import com.kumar.expenseease.databinding.FragmentExpenseBinding;

import java.util.ArrayList;
import java.util.List;

public class expenseFragment extends Fragment {

    FragmentExpenseBinding binding;
    private List<dataClass> dataClass;
    private expenseTransactionsAdapter transactionsAdapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String email;
    private Dialog loadingDialog, dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExpenseBinding.inflate(inflater, container, false);
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
        binding.recyclerViewExpense.setLayoutManager(layoutManager);

        // Initialize the dataClass list and the transactionsAdapter
        dataClass = new ArrayList<>();
        transactionsAdapter = new expenseTransactionsAdapter(dataClass, new expenseTransactionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.kumar.expenseease.dataModels.dataClass dataClass, int position) {
                openDialog(dataClass);
            }
        }, new expenseTransactionsAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(com.kumar.expenseease.dataModels.dataClass dataClass, int position) {

            }
        });

        fetchExpenseTransactions();

        // Set the adapter to the RecyclerView
        binding.recyclerViewExpense.setAdapter(transactionsAdapter);
    }

    private void fetchExpenseTransactions() {
        dataClass.clear(); // Clear the existing list

        // Fetch expense transactions
        db.collection("expense")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadingDialog.dismiss();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("imageUrl");
                            String title = document.getString("expenseName");
                            String date = document.getString("expenseDate");
                            String category = document.getString("expenseCategory");
                            String documentId = document.getId();
                            double amount = document.getDouble("expenseAmount");
                            dataClass.add(new dataClass(title, date, imageUrl, category, documentId, amount, false));
                        }

                        transactionsAdapter.notifyDataSetChanged();
                        updateUI();

                    } else {
                        Toast.makeText(getContext(), "Failed to load expense transactions!", Toast.LENGTH_SHORT).show();
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
            binding.recyclerViewExpense.setVisibility(View.GONE);
            binding.emptyLayoutExpense.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewExpense.setVisibility(View.VISIBLE);
            binding.emptyLayoutExpense.setVisibility(View.GONE);
        }
    }
}