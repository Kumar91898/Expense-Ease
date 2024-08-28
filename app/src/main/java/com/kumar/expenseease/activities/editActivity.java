package com.kumar.expenseease.activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kumar.expenseease.R;
import com.kumar.expenseease.databinding.ActivityEditBinding;

import java.util.HashMap;

public class editActivity extends AppCompatActivity {
    ActivityEditBinding binding;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private String name, email, password, contact, imageUrl;
    private Uri imageUri;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        openLoadingDialogue();

        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        contact = getIntent().getStringExtra("contact");
        password = getIntent().getStringExtra("password");
        imageUrl = getIntent().getStringExtra("imageUrl");

        if (contact == null || contact.isEmpty()){
            binding.contactFieldEdit.setText("null");
        } else {
            binding.contactFieldEdit.setText(contact);
        }

        if (password == null || password.isEmpty()){
            binding.passwordFieldEdit.setText("Signed in with google");
        } else {
            binding.passwordFieldEdit.setText(password);
        }

        binding.backAccount.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.nameFieldEdit.setText(name);
        binding.emailFieldEdit.setText(email);
        Glide.with(this).load(imageUrl).into(binding.profileEdit);

        binding.changeImageEdit.setOnClickListener(v -> {
            selectImage();
        });

        binding.editProfileButton.setOnClickListener(v -> {
            loadingDialog.show();
            String name = binding.nameFieldEdit.getText().toString();
            String contact = binding.contactFieldEdit.getText().toString();

            if (imageUri == null){
                addDataWithoutImage(name, contact);
            } else {
                uploadImage(name, contact);
            }
        });
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
            binding.profileEdit.setImageURI(imageUri);
        }
    }

    private void uploadImage(String name, String contact){
        storageReference = FirebaseStorage.getInstance().getReference("profiles/" + name);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                loadingDialog.dismiss();
                                String imageURL = uri.toString();
                                addData(imageURL, name, contact);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(editActivity.this, "Failed to upload!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addData(String imageURL, String name, String contact) {

        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("contact", contact);
        data.put("imageUrl", imageURL);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            loadingDialog.dismiss();
                            for (QueryDocumentSnapshot document : task.getResult()){
                                String documentId = document.getId();

                                db.collection("users")
                                        .document(documentId)
                                        .update(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(editActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void addDataWithoutImage(String name, String contact) {

        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("contact", contact);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            loadingDialog.dismiss();
                            for (QueryDocumentSnapshot document : task.getResult()){
                                String documentId = document.getId();

                                db.collection("users")
                                        .document(documentId)
                                        .update(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(editActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void openLoadingDialogue(){
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialogue_box);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        loadingDialog.setCancelable(false);
    }
}