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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kumar.expenseease.R;
import com.kumar.expenseease.adapters.allTransactionsAdapter;
import com.kumar.expenseease.adapters.transactionsAdapter;
import com.kumar.expenseease.dataModels.dataClass;
import com.kumar.expenseease.databinding.FragmentAllBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class allFragment extends Fragment {

    FragmentAllBinding binding;
    private List<dataClass> dataClass;
    private allTransactionsAdapter transactionsAdapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String email;
    private double incomeAmount, expenseAmount;
    private Dialog loadingDialog, dialog, removeDialog;
    private double ShoppingAmountSpent, FoodSpentAmount, EntertainmentSpentAmount, LoanSpentAmount, RentSpentAmount;
    private double shoppingSpent, foodSpent, entertainmentSpent, rentSpent, loanSpent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAllBinding.inflate(inflater, container, false);
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
        binding.recyclerViewAll.setLayoutManager(layoutManager);

        // Initialize the dataClass list and the transactionsAdapter
        dataClass = new ArrayList<>();
        transactionsAdapter = new allTransactionsAdapter(dataClass, new allTransactionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.kumar.expenseease.dataModels.dataClass dataClass, int position) {
                openDialog(dataClass);
            }
        }, new allTransactionsAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(com.kumar.expenseease.dataModels.dataClass dataClass, int position) {
                confirmDialog(dataClass);
            }
        });

        fetchTransactions();
        fetchData();

        // Set the adapter to the RecyclerView
        binding.recyclerViewAll.setAdapter(transactionsAdapter);
    }

    private void fetchTransactions() {
        dataClass.clear(); // Clear existing list

        // Fetch income transactions
        fetchIncomeTransactions();

        // Fetch expense transactions
        fetchExpenseTransactions();
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

    private void confirmDialog(dataClass dataClass){
        removeDialog = new Dialog(getContext());
        removeDialog.setContentView(R.layout.remove_layout);
        removeDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        removeDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        removeDialog.setCancelable(false);
        removeDialog.show();

        com.google.android.material.button.MaterialButton removeButton = removeDialog.findViewById(R.id.removeButton);
        com.google.android.material.button.MaterialButton cancelButton = removeDialog.findViewById(R.id.cancelButton_remove);

        removeButton.setOnClickListener(v -> {


            removeTransactions(dataClass);
        });

        cancelButton.setOnClickListener(v -> removeDialog.dismiss());
    }

    private void fetchData(){
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                incomeAmount = documentSnapshot.getDouble("incomeAmount");
                                expenseAmount = documentSnapshot.getDouble("expenseAmount");
                                FoodSpentAmount = documentSnapshot.getDouble("foodSpent");
                                ShoppingAmountSpent = documentSnapshot.getDouble("shoppingSpent");
                                EntertainmentSpentAmount = documentSnapshot.getDouble("entertainmentSpent");
                                LoanSpentAmount = documentSnapshot.getDouble("loanSpent");
                                RentSpentAmount = documentSnapshot.getDouble("rentSpent");
                            }
                        }
                    }
                });
    }

    private void removeTransactions(dataClass dataClass){
        String collection;

        if (dataClass.isIncome()){
            collection = "income";

            String category = dataClass.getCategory();

            if ("Shopping".equals(category)) {
                shoppingSpent = ShoppingAmountSpent - dataClass.getAmount();
            }
            if ("Food & Drinks".equals(category)) {
                foodSpent = FoodSpentAmount - dataClass.getAmount();
            }
            if ("Entertainment".equals(category)) {
                entertainmentSpent = EntertainmentSpentAmount - dataClass.getAmount();
            }
            if ("Loan".equals(category)) {
                loanSpent = LoanSpentAmount - dataClass.getAmount();
            }
            if ("Rent".equals(category)) {
                rentSpent = RentSpentAmount - dataClass.getAmount();
            }

            updateIncomeData(dataClass);
        } else {
            collection = "expense";
            updateExpenseData(collection, dataClass);
        }

        db.collection(collection)
                .document(dataClass.getDocumentId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        removeDialog.dismiss();
                        Toast.makeText(getContext(), "Transaction removed!", Toast.LENGTH_SHORT).show();
                        fetchTransactions();
                    }
                });
    }

    private void updateIncomeData(dataClass dataClass){
        double newIncome = incomeAmount - dataClass.getAmount();

        HashMap<String, Object> newData = new HashMap<>();
        newData.put("incomeAmount", newIncome);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                String documentId = documentSnapshot.getId();

                                db.collection("users")
                                        .document(documentId)
                                        .update(newData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getContext(), "Balance updated!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void updateExpenseData(String collection, dataClass dataClass){
        double newExpense = expenseAmount - dataClass.getAmount();

        HashMap<String, Object> newData = new HashMap<>();
        newData.put("expenseAmount", newExpense);
        newData.put("foodSpent", foodSpent);
        newData.put("entertainmentSpent", entertainmentSpent);
        newData.put("shoppingSpent", shoppingSpent);
        newData.put("rentSpent", rentSpent);
        newData.put("loanSpent", loanSpent);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                String documentId = documentSnapshot.getId();

                                db.collection("users")
                                        .document(documentId)
                                        .update(newData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getContext(), "Balance updated!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void updateUI() {
        if (dataClass.isEmpty()) {
            binding.recyclerViewAll.setVisibility(View.GONE);
            binding.emptyLayoutAll.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewAll.setVisibility(View.VISIBLE);
            binding.emptyLayoutAll.setVisibility(View.GONE);
        }
    }
}