package com.kumar.expenseease.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kumar.expenseease.R;
import com.kumar.expenseease.databinding.ActivityLoginScreenBinding;

public class loginScreen extends AppCompatActivity {
    ActivityLoginScreenBinding binding;
    private FirebaseAuth authentication;
    private Dialog dialog, loadingDialog;

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = authentication.getCurrentUser();
        if (currentUser != null){
            startActivity(new Intent(this, homeScreen.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authentication = FirebaseAuth.getInstance();

        binding.registerText.setOnClickListener(v -> {
            binding.registerText.setTextColor(getColor(R.color.secondaryColor));
            startActivity(new Intent(this, registerScreen.class));
            Animatoo.INSTANCE.animateSwipeLeft(this);
            finish();
        });

        binding.loginButton.setOnClickListener(v -> {
            binding.buttonAnimationLogin.setVisibility(View.VISIBLE);
            binding.buttonAnimationLogin.playAnimation();

            binding.buttonTextLogin.setVisibility(View.GONE);
            binding.loginButton.setEnabled(false);

            loginWithEmail();
        });

        binding.forgotText.setOnClickListener(v -> {
            openForgotDialog();
        });
    }

    private void openForgotDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.forgot_sheet);

        com.google.android.material.textfield.TextInputEditText emailField = dialog.findViewById(R.id.forgot_email);
        RelativeLayout sendButton = dialog.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            openLoadingDialogue();
            String email = emailField.getText().toString();

            sendResetLink(email);
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void loginWithEmail() {
        String email = binding.emailField.getText().toString();
        String password = binding.passwordField.getText().toString();

        if (email.isEmpty()){
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            resetButton();
            return;
        }

        if (password.isEmpty()){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            resetButton();
            return;
        }

        authentication.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        resetButton();
                        if (task.isSuccessful()){
                            startActivity(new Intent(loginScreen.this, homeScreen.class));
                            finish();

                            Toast.makeText(loginScreen.this, "Logged In!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(loginScreen.this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendResetLink(String email){
        authentication.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        resetButton();
                        if (task.isSuccessful()){
                            loadingDialog.dismiss();
                            dialog.dismiss();
                            Toast.makeText(loginScreen.this, "Reset link send!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(loginScreen.this, "Failed to send!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void resetButton(){
        binding.buttonAnimationLogin.pauseAnimation();
        binding.buttonAnimationLogin.setVisibility(View.GONE);

        binding.buttonTextLogin.setVisibility(View.VISIBLE);
        binding.loginButton.setEnabled(true);
    }

    private void openLoadingDialogue(){
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialogue_box);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }
}