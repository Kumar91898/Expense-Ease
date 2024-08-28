package com.kumar.expenseease.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kumar.expenseease.R;
import com.kumar.expenseease.databinding.ActivityRegisterScreenBinding;

import java.util.HashMap;

public class registerScreen extends AppCompatActivity {
    ActivityRegisterScreenBinding binding;
    private FirebaseAuth authentication;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private Uri imageUri;
    private String email, password, name, contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authentication = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.loginText.setOnClickListener(v -> {
            binding.loginText.setTextColor(getColor(R.color.secondaryColor));
            startActivity(new Intent(this, loginScreen.class));
            Animatoo.INSTANCE.animateSwipeRight(this);
            finish();
        });

        binding.addImage.setOnClickListener(v -> {
            selectImage();
        });

        binding.registerButton.setOnClickListener(v -> {
            if (validateField()){
                binding.buttonAnimationRegister.setVisibility(View.VISIBLE);
                binding.buttonAnimationRegister.playAnimation();

                binding.buttonTextRegister.setVisibility(View.GONE);
                binding.registerButton.setEnabled(false);

                signUp();
            }
        });
    }

    private boolean validateField(){
        name = binding.nameFieldRegister.getText().toString();
        email = binding.emailFieldRegister.getText().toString();
        password = binding.passwordFieldRegister.getText().toString();
        contact = binding.contactFieldRegister.getText().toString();

        if (name.isEmpty()){
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        if (email.isEmpty()){
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        if (password.isEmpty()){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        if (contact.isEmpty()){
            Toast.makeText(this, "Enter Contact", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        if (imageUri == null){
            Toast.makeText(this, "Select image", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        return true;
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null){
            imageUri = data.getData();
            binding.profileRegister.setImageURI(imageUri);
        }
    }

    private void uploadImage(){
        storageReference = FirebaseStorage.getInstance().getReference("profiles/" + name);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageURL = uri.toString();
                                addData(imageURL);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(registerScreen.this, "Failed to upload!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addData(String imageURL){
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("password", password);
        data.put("contact", contact);
        data.put("imageUrl", imageURL);
        data.put("expenseAmount", 0);
        data.put("incomeAmount", 0);
        data.put("shoppingSpent", 0);
        data.put("foodSpent", 0);
        data.put("entertainmentSpent", 0);
        data.put("loanSpent", 0);
        data.put("rentSpent", 0);

        db.collection("users")
                .add(data);
    }

    private void signUp() {
        authentication.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            uploadImage();

                            Toast.makeText(registerScreen.this, "Account registered!", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(registerScreen.this, homeScreen.class));
                            finish();
                        } else {
                            Toast.makeText(registerScreen.this, "Check your internet connection!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void resetButton() {
        binding.buttonAnimationRegister.pauseAnimation();
        binding.buttonAnimationRegister.setVisibility(View.GONE);

        binding.buttonTextRegister.setVisibility(View.VISIBLE);
        binding.registerButton.setEnabled(true);
    }
}