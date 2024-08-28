package com.kumar.expenseease.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.kumar.expenseease.activities.MainActivity;
import com.kumar.expenseease.activities.editActivity;
import com.kumar.expenseease.databinding.FragmentAccountBinding;

public class accountFragment extends Fragment {

    FragmentAccountBinding binding;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Dialog dialog, photoDialog, loadingDialog;
    private String email, imageUrl, name, contact, password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        email = currentUser.getEmail();

        openLoadingDialogue();

        fetchData();

        binding.userPhoto.setOnClickListener(v -> {
            openPhotoDialog();
        });

        binding.details.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), editActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("name", name);
            intent.putExtra("password", password);
            intent.putExtra("contact", contact);
            intent.putExtra("imageUrl", imageUrl);
            startActivity(intent);
        });

        binding.support.setOnClickListener(v -> {
            new Handler().postDelayed(this::gotoSupport, 800);
        });

        binding.logout.setOnClickListener(v -> {
            confirmDialog();
        });

        refreshFragment();
    }

    private void gotoSupport(){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.accountFrame, new supportFragment());
        fragmentTransaction.commit();
    }

    private void openPhotoDialog() {
        photoDialog = new Dialog(getContext());
        photoDialog.setContentView(R.layout.photo_dialog);
        photoDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        photoDialog.getWindow().setBackgroundDrawableResource(R.drawable.photo_dialog_bg);
        photoDialog.setCancelable(true);
        photoDialog.show();

        ImageView imageView = photoDialog.findViewById(R.id.userPhoto_info);
        Glide.with(getContext()).load(imageUrl).into(imageView);
    }

    private void fetchData(){
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            loadingDialog.dismiss();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                name = documentSnapshot.getString("name");
                                contact = documentSnapshot.getString("contact");
                                password = documentSnapshot.getString("password");
                                imageUrl = documentSnapshot.getString("imageUrl");

                                binding.userName.setText(name);
                                binding.userEmail.setText(email);
                                Glide.with(getContext()).load(imageUrl).into(binding.userPhoto);
                            }
                        }
                    }
                });
    }

    private void confirmDialog(){
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.logout_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        dialog.setCancelable(false);
        dialog.show();

        com.google.android.material.button.MaterialButton logoutButton = dialog.findViewById(R.id.logoutButton);
        com.google.android.material.button.MaterialButton exitButton = dialog.findViewById(R.id.exitButton_logout);

        logoutButton.setOnClickListener(v -> signOut());

        exitButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        startActivity(new Intent(getContext(), MainActivity.class));
        getActivity().finish();
    }

    private void openLoadingDialogue(){
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_dialogue_box);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    private void refreshFragment() {
        binding.swipeAccount.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();

                Toast.makeText(getContext(), "Page Refreshed!", Toast.LENGTH_SHORT).show();
                binding.swipeAccount.setRefreshing(false);
            }
        });
    }
}