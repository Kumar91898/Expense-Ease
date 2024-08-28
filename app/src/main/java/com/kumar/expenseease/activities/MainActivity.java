package com.kumar.expenseease.activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kumar.expenseease.R;
import com.kumar.expenseease.activities.homeScreen;
import com.kumar.expenseease.activities.loginScreen;
import com.kumar.expenseease.adapters.ViewPagerAdapter;
import com.kumar.expenseease.dataModels.NotificationHelper;
import com.kumar.expenseease.databinding.ActivityMainBinding;
import com.kumar.expenseease.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private FirebaseFirestore db;
    private GoogleSignInClient gsc;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 1;
    private int currentPage = 0;
    private int progress = 0;
    private Handler handler = new Handler();
    private Runnable runnable;
    private Dialog dialog;

    @Override
    public void onStart() {
        super.onStart();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(this, homeScreen.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!NetworkUtil.isNetworkConnected(this)){
            showNoInternetDialog();
        } else {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            List<View> views = new ArrayList<>();
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            views.add(layoutInflater.inflate(R.layout.screen1, null));
            views.add(layoutInflater.inflate(R.layout.screen2, null));

            binding.viewPager.setAdapter(new ViewPagerAdapter(views));
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Configure Google sign-in
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            gsc = GoogleSignIn.getClient(this, gso);

            startProgress();

            binding.emailButton.setOnClickListener(v -> {
                startActivity(new Intent(this, loginScreen.class));
                Animatoo.INSTANCE.animateZoom(this);
            });

            binding.googleButton.setOnClickListener(v -> {
                signupWithGoogle();
            });
        }
    }

    private void startProgress() {
        runnable = new Runnable() {
            @Override
            public void run() {
                progress++;
                if (currentPage == 0) {
                    binding.progressBar1.setProgress(progress);
                } else if (currentPage == 1) {
                    binding.progressBar2.setProgress(progress);
                }

                if (progress < 100) {
                    handler.postDelayed(this, 40); // Adjust timing as needed
                } else {
                    progress = 0;
                    currentPage++;
                    if (currentPage >= binding.viewPager.getAdapter().getCount()) {
                        currentPage = 0;
                        binding.viewPager.setCurrentItem(currentPage, true); // Loop back to the first screen
                    } else {
                        binding.viewPager.setCurrentItem(currentPage);
                    }
                    startProgress();
                }
            }
        };
        handler.post(runnable);
    }

    public void signupWithGoogle() {
        gsc.signOut().addOnCompleteListener(this, task -> {
            Intent signupIntent = gsc.getSignInIntent();
            startActivityForResult(signupIntent, RC_SIGN_IN);
        });
        openDialogue();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String getEmail = account.getEmail();
                    String getPhoto = String.valueOf(account.getPhotoUrl());
                    String getName = account.getDisplayName();
                    firebaseAuthWithGoogle(account.getIdToken(), getName, getEmail, getPhoto);
                }
            } catch (ApiException e) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, String getName, String getEmail, String getUrl) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            addData(getName, getEmail, getUrl);
                        } else {
                            Toast.makeText(MainActivity.this, "Firebase Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void addData(String getName, String getEmail, String imageUrl) {
        Map<String, Object> user_data = new HashMap<>();
        user_data.put("name", getName);
        user_data.put("email", getEmail);
        user_data.put("contact", "");
        user_data.put("imageUrl", imageUrl);
        user_data.put("expenseAmount", 0);
        user_data.put("incomeAmount", 0);
        user_data.put("shoppingSpent", 0);
        user_data.put("foodSpent", 0);
        user_data.put("entertainmentSpent", 0);
        user_data.put("loanSpent", 0);
        user_data.put("rentSpent", 0);

        db.collection("users")
                .whereEqualTo("email", getEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        if (task.getResult().isEmpty()){
                            db.collection("users")
                                    .add(user_data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            moveToHomeScreen();
                                            Toast.makeText(MainActivity.this, "Logged In!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            moveToHomeScreen();
                            Toast.makeText(this, "Logged In!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openDialogue(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_dialogue_box);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void moveToHomeScreen() {
        Intent intent = new Intent(MainActivity.this, homeScreen.class);
        startActivity(intent);
        finish();
        dialog.dismiss();
    }

    private void showNoInternetDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.internet_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        dialog.setCancelable(true);
        dialog.show();

        // Find buttons in the custom view and set click listeners
        Button retryButton = dialog.findViewById(R.id.retryButton);
        Button exitButton = dialog.findViewById(R.id.exitButton);

        retryButton.setOnClickListener(v -> {
            // Retry button clicked, check the connection again
            if (NetworkUtil.isNetworkConnected(this)) {
                // Internet is available, reload the activity
                dialog.dismiss();
                finish();
                startActivity(getIntent());
            } else {
                // Show a toast message
                Toast.makeText(this, "Still no internet connection", Toast.LENGTH_SHORT).show();
            }
        });

        exitButton.setOnClickListener(v -> {
            // Exit the app
            dialog.dismiss();
            finish();
        });

        // Show the custom dialog
        dialog.setCancelable(false);
        dialog.show();
    }
}
