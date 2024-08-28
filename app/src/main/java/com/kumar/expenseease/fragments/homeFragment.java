package com.kumar.expenseease.fragments;

import android.app.Dialog;
import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kumar.expenseease.R;
import com.kumar.expenseease.activities.transactionsActivity;
import com.kumar.expenseease.adapters.transactionsAdapter;
import com.kumar.expenseease.dataModels.dataClass;
import com.kumar.expenseease.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class homeFragment extends Fragment {

    FragmentHomeBinding binding;
    private List<dataClass> dataClass;
    private transactionsAdapter transactionsAdapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String email, category;
    private Dialog dialog, loadingDialog;
    private double ShoppingAmountSpent, FoodSpentAmount, EntertainmentSpentAmount, LoanSpentAmount, RentSpentAmount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the RecyclerView with a LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        email = currentUser.getEmail();

        openLoadingDialogue();

        // Initialize the dataClass list and the transactionsAdapter
        dataClass = new ArrayList<>();
        transactionsAdapter = new transactionsAdapter(dataClass, new transactionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.kumar.expenseease.dataModels.dataClass dataClass, int position) {
                openDialog(dataClass);
            }
        }, new transactionsAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(com.kumar.expenseease.dataModels.dataClass dataClass, int position) {
                Toast.makeText(getContext(), "To remove the transaction go to all transaction tab!", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch all transactions
        fetchTransactions();
        fetchExpenseAndIncome();
        binding.dateHome.setText(getTodayDate());

        // Set the adapter to the RecyclerView
        binding.recyclerView.setAdapter(transactionsAdapter);

        // Fetch transactions by categories
        manageCategories();

        binding.transactionLayout.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), transactionsActivity.class));
        });
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

    private void fetchTransactions() {
        dataClass.clear(); // Clear existing list

        // Fetch expense transactions
        fetchExpenseTransactions();

        // Fetch income transactions
        fetchIncomeTransactions();
    }

    private void fetchExpenseAndIncome() {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        loadingDialog.dismiss();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            double totalExpense = documentSnapshot.getDouble("expenseAmount");
                            double totalIncome = documentSnapshot.getDouble("incomeAmount");

                            binding.availableBalaneHome.setText("Rs "+(totalIncome-totalExpense));
                            binding.totalExpenseHome.setText("Rs "+totalExpense);
                            binding.totalIncomeHome.setText("Rs "+totalIncome);
                        }
                    }
                });
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

                            // Add to total shopping amount if expense category is "Shopping"
                            if ("Shopping".equals(document.getString("expenseCategory"))) {
                                ShoppingAmountSpent += amount;
                            }
                            if ("Food & Drinks".equals(document.getString("expenseCategory"))) {
                                FoodSpentAmount += amount;
                            }
                            if ("Entertainment".equals(document.getString("expenseCategory"))) {
                                EntertainmentSpentAmount += amount;
                            }
                            if ("Loan".equals(document.getString("expenseCategory"))) {
                                LoanSpentAmount += amount;
                            }
                            if ("Rent".equals(document.getString("expenseCategory"))) {
                                RentSpentAmount += amount;
                            }

                            addToUsersCollection();
                        }

                        transactionsAdapter.notifyDataSetChanged();
                        updateUI();

                    } else {
                        Toast.makeText(getContext(), "Failed to load expense transactions!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToUsersCollection(){

        HashMap<String, Object> data = new HashMap<>();
        data.put("shoppingSpent", ShoppingAmountSpent);
        data.put("foodSpent", FoodSpentAmount);
        data.put("entertainmentSpent", EntertainmentSpentAmount);
        data.put("loanSpent", LoanSpentAmount);
        data.put("rentSpent", RentSpentAmount);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            loadingDialog.dismiss();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                String documentId = documentSnapshot.getId();

                                db.collection("users")
                                        .document(documentId)
                                        .update(data);
                            }
                        }
                    }
                });
    }

    private void fetchIncomeTransactions() {
        dataClass.clear();

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

    private void manageCategories() {
        binding.food.setOnClickListener(v -> {
            resetCategoryBackgrounds();
            binding.food.setBackgroundResource(R.drawable.category_ripple);
            category = "Food & Drinks";
            fetchTransactionsByCategory(category);
        });

        binding.entertainment.setOnClickListener(v -> {
            resetCategoryBackgrounds();
            binding.entertainment.setBackgroundResource(R.drawable.category_ripple);
            category = "Entertainment";
            fetchTransactionsByCategory(category);
        });

        binding.shopping.setOnClickListener(v -> {
            resetCategoryBackgrounds();
            binding.shopping.setBackgroundResource(R.drawable.category_ripple);
            category = "Shopping";
            fetchTransactionsByCategory(category);
        });

        binding.loan.setOnClickListener(v -> {
            resetCategoryBackgrounds();
            binding.loan.setBackgroundResource(R.drawable.category_ripple);
            category = "Loan";
            fetchTransactionsByCategory(category);
        });

        binding.rent.setOnClickListener(v -> {
            resetCategoryBackgrounds();
            binding.rent.setBackgroundResource(R.drawable.category_ripple);
            category = "Rent";
            fetchTransactionsByCategory(category);
        });
    }

    private void resetCategoryBackgrounds() {
        binding.food.setBackgroundResource(R.color.white);
        binding.entertainment.setBackgroundResource(R.color.white);
        binding.shopping.setBackgroundResource(R.color.white);
        binding.loan.setBackgroundResource(R.color.white);
        binding.rent.setBackgroundResource(R.color.white);
    }

    private void fetchTransactionsByCategory(String category){
        dataClass.clear(); // Clear the existing list

        // Fetch expense transactions by category
        db.collection("expense")
                .whereEqualTo("email", email)
                .whereEqualTo("expenseCategory", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadingDialog.dismiss();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("imageUrl");
                            String title = document.getString("expenseName");
                            String date = document.getString("expenseDate");
                            String documentId = document.getId();
                            String fetchCategory = document.getString("expenseCategory");
                            double amount = document.getDouble("expenseAmount");
                            dataClass.add(new dataClass(title, date, imageUrl, fetchCategory, documentId, amount, false));
                        }

                        transactionsAdapter.notifyDataSetChanged();
                        updateUI();

                    } else {
                        Toast.makeText(getContext(), "Failed to load transactions by category!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (dataClass.isEmpty()) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyLayout.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyLayout.setVisibility(View.GONE);
        }
    }

    public static String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM");
        return dateFormat.format(calendar.getTime());
    }

    private void openLoadingDialogue(){
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_dialogue_box);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }
}
