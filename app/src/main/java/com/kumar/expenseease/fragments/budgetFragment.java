package com.kumar.expenseease.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kumar.expenseease.R;
import com.kumar.expenseease.adapters.budgetAdapter;
import com.kumar.expenseease.adapters.transactionsAdapter;
import com.kumar.expenseease.dataModels.NotificationHelper;
import com.kumar.expenseease.dataModels.budgetClass;
import com.kumar.expenseease.databinding.FragmentBudgetBinding;
import com.kumar.expenseease.utils.DrawableUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class budgetFragment extends Fragment {

    FragmentBudgetBinding binding;
    private FirebaseFirestore db;
    private List<budgetClass> budgetClass;
    private budgetAdapter budgetAdapter;
    private FirebaseUser CurrentUser;
    private String email;
    private RelativeLayout addBudget, editBudget;
    private TextView budget_text, edit_budget_text;
    private LottieAnimationView budgetAnimation, edit_budget_animation;
    private Dialog dialog, editDialog, loadingDialog;
    private String category;
    private Uri imageUri;
    private double shoppingSpent, foodSpent, entertainmentSpent, loanSpent, rentSpent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the RecyclerView with a LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewBudget.setLayoutManager(layoutManager);

        db = FirebaseFirestore.getInstance();
        CurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        email = CurrentUser.getEmail();

        openLoadingDialogue();

        fetchBalance();
        fetchBudgetData();

        // Initialize the dataClass list and the transactionsAdapter
        budgetClass = new ArrayList<>();
        budgetAdapter = new budgetAdapter(budgetClass, new budgetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.kumar.expenseease.dataModels.budgetClass budgetClass, int position) {

                openBudgetEditDialog(budgetClass);

            }
        });


        // Set the adapter to the RecyclerView
        binding.recyclerViewBudget.setAdapter(budgetAdapter);

        binding.createBudgetButton.setOnClickListener(v -> {
            openDialog();
        });

        refreshFragment();
    }

    private void openBudgetEditDialog(budgetClass budgetClass) {
        editDialog = new Dialog(getContext());
        editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editDialog.setContentView(R.layout.edit_budget_sheet);

        com.google.android.material.textfield.TextInputEditText amountField = editDialog.findViewById(R.id.edit_budget_amount);
        editBudget = editDialog.findViewById(R.id.edit_budget_button);
        edit_budget_text = editDialog.findViewById(R.id.edit_budget_text);
        edit_budget_animation = editDialog.findViewById(R.id.edit_budget_animation);
        ImageView imageView = editDialog.findViewById(R.id.edit_image_budget);
        TextView category = editDialog.findViewById(R.id.edit_category);

        Glide.with(getContext()).load(budgetClass.getImageUrl()).into(imageView);
        amountField.setText(String.valueOf(budgetClass.getTotalBudget()));
        category.setText(budgetClass.getCategory());

        editBudget.setOnClickListener(v -> {
            edit_budget_animation.setVisibility(View.VISIBLE);
            edit_budget_animation.playAnimation();
            edit_budget_text.setVisibility(View.GONE);
            editBudget.setEnabled(false);

            String amount = amountField.getText().toString();

            double getBudgetAmount;
            try {
                getBudgetAmount = Double.parseDouble(amount);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                resetEditButton();
                editDialog.dismiss();
                return;
            }

            updateBudget(budgetClass.getCategory(), getBudgetAmount);
        });

        editDialog.show();
        editDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        editDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void openDialog() {
        dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_budget_sheet);

        LinearLayout foodCard = dialog.findViewById(R.id.foodCategory);
        LinearLayout entertainmentCard = dialog.findViewById(R.id.entertainment_Category);
        LinearLayout shoppingCard = dialog.findViewById(R.id.shopping_Category);
        LinearLayout loanCard = dialog.findViewById(R.id.loan_Category);
        LinearLayout rentCard = dialog.findViewById(R.id.rent_Category);

        com.google.android.material.textfield.TextInputEditText amountField = dialog.findViewById(R.id.budget_amount);
        addBudget = dialog.findViewById(R.id.budget_button);
        budget_text = dialog.findViewById(R.id.budget_text);
        budgetAnimation = dialog.findViewById(R.id.budget_animation);

        foodCard.setOnClickListener(v -> {
            foodCard.setBackgroundResource(R.drawable.category_ripple);
            entertainmentCard.setBackgroundResource(R.color.white);
            shoppingCard.setBackgroundResource(R.color.white);
            loanCard.setBackgroundResource(R.color.white);
            rentCard.setBackgroundResource(R.color.white);
            category = "Food & Drinks";

            imageUri = DrawableUtil.getDrawableUri(getContext(), R.drawable.food_expense);
        });

        entertainmentCard.setOnClickListener(v -> {
            foodCard.setBackgroundResource(R.color.white);
            entertainmentCard.setBackgroundResource(R.drawable.category_ripple);
            shoppingCard.setBackgroundResource(R.color.white);
            loanCard.setBackgroundResource(R.color.white);
            rentCard.setBackgroundResource(R.color.white);
            category = "Entertainment";
            imageUri = DrawableUtil.getDrawableUri(getContext(), R.drawable.entertainment_expense);
        });

        shoppingCard.setOnClickListener(v -> {
            foodCard.setBackgroundResource(R.color.white);
            entertainmentCard.setBackgroundResource(R.color.white);
            shoppingCard.setBackgroundResource(R.drawable.category_ripple);
            loanCard.setBackgroundResource(R.color.white);
            rentCard.setBackgroundResource(R.color.white);
            category = "Shopping";
            imageUri = DrawableUtil.getDrawableUri(getContext(), R.drawable.shopping_expense);
        });

        loanCard.setOnClickListener(v -> {
            foodCard.setBackgroundResource(R.color.white);
            entertainmentCard.setBackgroundResource(R.color.white);
            shoppingCard.setBackgroundResource(R.color.white);
            loanCard.setBackgroundResource(R.drawable.category_ripple);
            rentCard.setBackgroundResource(R.color.white);
            category = "Loan";
            imageUri = DrawableUtil.getDrawableUri(getContext(), R.drawable.loan_expense);
        });

        rentCard.setOnClickListener(v -> {
            foodCard.setBackgroundResource(R.color.white);
            entertainmentCard.setBackgroundResource(R.color.white);
            shoppingCard.setBackgroundResource(R.color.white);
            loanCard.setBackgroundResource(R.color.white);
            rentCard.setBackgroundResource(R.drawable.category_ripple);
            category = "Rent";
            imageUri = DrawableUtil.getDrawableUri(getContext(), R.drawable.rent_expense);
        });
        
        addBudget.setOnClickListener(v -> {
            budgetAnimation.setVisibility(View.VISIBLE);
            budgetAnimation.playAnimation();
            budget_text.setVisibility(View.GONE);
            addBudget.setEnabled(false);

            String budgetAmount = amountField.getText().toString();

            if (budgetAmount.isEmpty()) {
                Toast.makeText(getContext(), "Please enter budget amount!", Toast.LENGTH_SHORT).show();
                resetButton();
                return;
            }
            
            double getBudgetAmount;
            try {
                getBudgetAmount = Double.parseDouble(budgetAmount);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                resetButton();
                return;
            }
            
            uploadImageToStorage(getBudgetAmount);
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void uploadImageToStorage(double budgetAmount) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("budget/" + category);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String firebaseImageUrl = uri.toString();

                        addBudget(budgetAmount, firebaseImageUrl);
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed To Update!", Toast.LENGTH_SHORT).show();
                });
    }

    private void addBudget(double budgetAmount, String firebaseImageUrl) {

        HashMap<String , Object> data = new HashMap<>();
        data.put("email", email);
        data.put("totalBudget", budgetAmount);
        data.put("category", category);
        data.put("imageUrl", firebaseImageUrl);
        
        db.collection("budget")
                .whereEqualTo("email", email)
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        if (task.getResult().isEmpty()){
                            db.collection("budget")
                                    .add(data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(getContext(), "Budget Added!", Toast.LENGTH_SHORT).show();
                                            fetchBalance();
                                            fetchBudgetData();
                                        }
                                    });
                        } else {
                            Toast.makeText(getContext(), "Budget already exists!", Toast.LENGTH_SHORT).show();
                        }
                        resetButton();
                        dialog.dismiss();
                    }
                });
    }

    private void fetchBudgetData(){
        db.collection("budget")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        budgetClass.clear();
                        if (task.isSuccessful()){
                            loadingDialog.dismiss();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                String category = documentSnapshot.getString("category");
                                String imageUrl = documentSnapshot.getString("imageUrl");
                                double totalBudget = documentSnapshot.getDouble("totalBudget");

                                if (category.equals("Food & Drinks")){
                                    budgetClass.add(new budgetClass(category, totalBudget, foodSpent, imageUrl));
                                }
                                if (category.equals("Shopping")){
                                    budgetClass.add(new budgetClass(category, totalBudget, shoppingSpent, imageUrl));
                                }
                                if (category.equals("Entertainment")){
                                    budgetClass.add(new budgetClass(category, totalBudget, entertainmentSpent, imageUrl));
                                }
                                if (category.equals("Loan")){
                                    budgetClass.add(new budgetClass(category, totalBudget, loanSpent, imageUrl));
                                }
                                if (category.equals("Rent")){
                                    budgetClass.add(new budgetClass(category, totalBudget, rentSpent, imageUrl));
                                }

                            }

                            budgetAdapter.notifyDataSetChanged();
                            updateUI();
                        }
                    }
                });
    }

    private void fetchBalance() {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            loadingDialog.dismiss();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                double incomeBalance = documentSnapshot.getDouble("incomeAmount");
                                double expenseBalance = documentSnapshot.getDouble("expenseAmount");
                                foodSpent = documentSnapshot.getDouble("foodSpent");
                                entertainmentSpent = documentSnapshot.getDouble("entertainmentSpent");
                                shoppingSpent = documentSnapshot.getDouble("shoppingSpent");
                                loanSpent = documentSnapshot.getDouble("loanSpent");
                                rentSpent = documentSnapshot.getDouble("rentSpent");

                                binding.availableBalance.setText("Rs "+(incomeBalance-expenseBalance));
                            }
                        }
                    }
                });
    }

    private void updateBudget(String category, double getBudgetAmount){

        HashMap<String, Object> data = new HashMap<>();
        data.put("totalBudget", getBudgetAmount);

        db.collection("budget")
                .whereEqualTo("email", email)
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                String documentId = documentSnapshot.getId();

                                db.collection("budget")
                                        .document(documentId)
                                        .update(data)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                budgetAdapter.notifyDataSetChanged();
                                                resetEditButton();
                                                editDialog.dismiss();
                                                Toast.makeText(getContext(), "Budget Updated!", Toast.LENGTH_SHORT).show();

                                                fetchBalance();
                                                fetchBudgetData();
                                            }
                                        });

                            }
                        }
                    }
                });
    }
    
    private void resetButton(){
        budgetAnimation.pauseAnimation();
        budgetAnimation.setVisibility(View.GONE);
        
        budget_text.setVisibility(View.VISIBLE);
        addBudget.setEnabled(true);
    }

    private void resetEditButton(){
        edit_budget_animation.pauseAnimation();
        edit_budget_animation.setVisibility(View.GONE);

        edit_budget_text.setVisibility(View.VISIBLE);
        editBudget.setEnabled(true);
    }

    private void refreshFragment() {
        binding.swipeBudget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchBudgetData();

                Toast.makeText(getContext(), "Page Refreshed!", Toast.LENGTH_SHORT).show();
                binding.swipeBudget.setRefreshing(false);
            }
        });
    }

    private void updateUI() {
        if (budgetClass.isEmpty()) {
            binding.recyclerViewBudget.setVisibility(View.GONE);
            binding.budgetEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewBudget.setVisibility(View.VISIBLE);
            binding.budgetEmptyLayout.setVisibility(View.GONE);
        }
    }

    private void openLoadingDialogue(){
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_dialogue_box);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        loadingDialog.setCancelable(true);
        loadingDialog.show();
    }
}