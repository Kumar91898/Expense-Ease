package com.kumar.expenseease.fragments;

import static androidx.browser.customtabs.CustomTabsClient.getPackageName;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.kumar.expenseease.databinding.FragmentWalletBinding;
import com.kumar.expenseease.utils.DrawableUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class walletFragment extends Fragment {
    
    FragmentWalletBinding binding;
    private Dialog incomeDialog, expenseDialog;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String email, category;
    private double income, expense, getExpenseAmount, getIncomeAmount;
    private RelativeLayout incomeButton, expenseButton;
    private Uri imageUri;
    private TextView buttonText_income, buttonText_expense;
    private LottieAnimationView buttonAnimation_income, buttonAnimation_expense;
    private Dialog loadingDialog;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWalletBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        email = currentUser.getEmail();

        openLoadingDialogue();

        fetchExpenseAndIncome();
        refreshFragment();

        binding.addIncomeLayout.setOnClickListener(v -> {
            openIncomeDialog();
        });

        binding.addExpenseLayout.setOnClickListener(v -> {
            openExpenseDialog();
        });
    }

    private void openIncomeDialog(){
        incomeDialog = new Dialog(getContext());
        incomeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        incomeDialog.setContentView(R.layout.add_income_sheet);

        com.google.android.material.textfield.TextInputEditText dateField = incomeDialog.findViewById(R.id.dateField);
        com.google.android.material.textfield.TextInputEditText nameField = incomeDialog.findViewById(R.id.income_name_Field);
        com.google.android.material.textfield.TextInputEditText amountField = incomeDialog.findViewById(R.id.income_amount);
        incomeButton = incomeDialog.findViewById(R.id.add_income_button);
        buttonText_income = incomeDialog.findViewById(R.id.button_text_add);
        buttonAnimation_income = incomeDialog.findViewById(R.id.button_animation_add);

        dateField.setOnClickListener(v -> {
            showDatePickerDialog(dateField);
        });

        incomeButton.setOnClickListener(v -> {
            buttonAnimation_income.setVisibility(View.VISIBLE);
            buttonAnimation_income.playAnimation();
            buttonText_income.setVisibility(View.GONE);
            incomeButton.setEnabled(false);

            String incomeName = nameField.getText().toString();
            String incomeAmount = amountField.getText().toString();
            String incomeDate = dateField.getText().toString();

            if (incomeName.isEmpty() || incomeAmount.isEmpty() || incomeDate.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                resetIncomeButton();
                return;
            }

            try {
                getIncomeAmount = Double.parseDouble(incomeAmount);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                resetIncomeButton();
                return;
            }

            uploadIncomeImageToStorage(incomeName, incomeDate, getIncomeAmount);
            addToUsersCollection();
        });

        incomeDialog.show();
        incomeDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        incomeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        incomeDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        incomeDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void openExpenseDialog(){
        expenseDialog = new Dialog(getContext());
        expenseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        expenseDialog.setContentView(R.layout.add_expense_sheet);

        LinearLayout foodCard = expenseDialog.findViewById(R.id.food);
        LinearLayout entertainmentCard = expenseDialog.findViewById(R.id.entertainment);
        LinearLayout shoppingCard = expenseDialog.findViewById(R.id.shopping);
        LinearLayout loanCard = expenseDialog.findViewById(R.id.loan);
        LinearLayout rentCard = expenseDialog.findViewById(R.id.rent);

        com.google.android.material.textfield.TextInputEditText expense_dateField = expenseDialog.findViewById(R.id.expense_dateField);
        com.google.android.material.textfield.TextInputEditText expense_nameField = expenseDialog.findViewById(R.id.expense_name_Field);
        com.google.android.material.textfield.TextInputEditText expense_amountField = expenseDialog.findViewById(R.id.expense_amount);
        expenseButton = expenseDialog.findViewById(R.id.add_expense_button);
        buttonText_expense = expenseDialog.findViewById(R.id.button_text_expense);
        buttonAnimation_expense = expenseDialog.findViewById(R.id.button_animation_expense);

        expense_dateField.setOnClickListener(v -> {
            showDatePickerDialog(expense_dateField);
        });

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

        expenseButton.setOnClickListener(v -> {
            buttonAnimation_expense.setVisibility(View.VISIBLE);
            buttonAnimation_expense.playAnimation();
            buttonText_expense.setVisibility(View.GONE);
            expenseButton.setEnabled(false);

            String expenseName = expense_nameField.getText().toString();
            String expenseAmount = expense_amountField.getText().toString();
            String expenseDate = expense_dateField.getText().toString();

            if (expenseName.isEmpty() || expenseAmount.isEmpty() || expenseDate.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                resetExpenseButton();
                return;
            }

            try {
                getExpenseAmount = Double.parseDouble(expenseAmount);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                resetExpenseButton();
                return;
            }

            uploadImageToStorage(expenseName, expenseDate, getExpenseAmount);
            addToUsersCollection();
        });

        expenseDialog.show();
        expenseDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        expenseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        expenseDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        expenseDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void showDatePickerDialog(com.google.android.material.textfield.TextInputEditText dateField) {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and set the date
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    // Format the date (e.g., "MM/dd/yyyy")
                    String selectedDate = (month1 + 1) + "/" + dayOfMonth + "/" + year1;
                    dateField.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void uploadImageToStorage(String expenseName, String expenseDate, double expenseAmount) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("categories/" + category);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String firebaseImageUrl = uri.toString();

                        addExpense(expenseName, expenseAmount, expenseDate, firebaseImageUrl);
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed To Update!", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadIncomeImageToStorage(String incomeName, String incomeDate, double incomeAmount) {
        Uri imageUri = DrawableUtil.getDrawableUri(getContext(), R.drawable.cash);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("income/" + "income");
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String firebaseImageUrl = uri.toString();

                        addIncome(incomeName, incomeAmount, incomeDate, firebaseImageUrl);
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed To Update!", Toast.LENGTH_SHORT).show();
                });
    }

    private void addIncome(String incomeName, double incomeAmount, String incomeDate, String firebaseImageUrl){
        HashMap<String, Object> expenseData = new HashMap<>();
        expenseData.put("email", email);
        expenseData.put("incomeName", incomeName);
        expenseData.put("incomeAmount", incomeAmount);
        expenseData.put("incomeDate", incomeDate);
        expenseData.put("imageUrl", firebaseImageUrl);

        db.collection("income")
                .add(expenseData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        resetIncomeButton();
                        Toast.makeText(getContext(), "Income Added!", Toast.LENGTH_SHORT).show();
                        incomeDialog.dismiss();
                        fetchExpenseAndIncome();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resetIncomeButton();
                        Toast.makeText(getContext(), "Income Added Failed!", Toast.LENGTH_SHORT).show();
                        incomeDialog.dismiss();
                    }
                });
    }

    private void addExpense(String expenseName, double expenseAmount, String expenseDate, String firebaseImageUrl){
        HashMap<String, Object> expenseData = new HashMap<>();
        expenseData.put("email", email);
        expenseData.put("expenseName", expenseName);
        expenseData.put("expenseAmount", expenseAmount);
        expenseData.put("expenseDate", expenseDate);
        expenseData.put("expenseCategory", category);
        expenseData.put("imageUrl", firebaseImageUrl);

        db.collection("expense")
                .add(expenseData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        resetExpenseButton();
                        Toast.makeText(getContext(), "Expense Added!", Toast.LENGTH_SHORT).show();
                        expenseDialog.dismiss();
                        fetchExpenseAndIncome();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resetExpenseButton();
                        Toast.makeText(getContext(), "Expense Added Failed!", Toast.LENGTH_SHORT).show();
                        expenseDialog.dismiss();
                    }
                });
    }

    private void addToUsersCollection(){
        double newExpenseAmount = expense+getExpenseAmount;
        double newIncomeAmount = income+getIncomeAmount;

        HashMap<String, Object> amountData = new HashMap<>();
        amountData.put("expenseAmount", newExpenseAmount);
        amountData.put("incomeAmount", newIncomeAmount);

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
                                        .update(amountData);
                            }
                        }
                    }
                });
    }

    private void fetchExpenseAndIncome(){
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            loadingDialog.dismiss();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                expense = documentSnapshot.getDouble("expenseAmount");
                                income = documentSnapshot.getDouble("incomeAmount");
                                binding.expenseText.setText("Rs "+expense);
                                binding.incomeText.setText("Rs "+income);
                                binding.avlbBalance.setText("Rs "+(income-expense));
                            }
                        }
                    }
                });
    }

    private void resetIncomeButton(){
        buttonAnimation_income.setVisibility(View.GONE);
        buttonAnimation_income.pauseAnimation();;

        buttonText_income.setVisibility(View.VISIBLE);
        incomeButton.setEnabled(true);
    }

    private void resetExpenseButton(){
        buttonAnimation_expense.setVisibility(View.GONE);
        buttonAnimation_expense.pauseAnimation();;

        buttonText_expense.setVisibility(View.VISIBLE);
        expenseButton.setEnabled(true);
    }

    private void refreshFragment() {
        binding.swipeWallet.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchExpenseAndIncome();

                Toast.makeText(getContext(), "Page Refreshed!", Toast.LENGTH_SHORT).show();
                binding.swipeWallet.setRefreshing(false);
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
}