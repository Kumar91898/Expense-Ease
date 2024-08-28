package com.kumar.expenseease.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kumar.expenseease.R;
import com.kumar.expenseease.databinding.FragmentSupportBinding;

public class supportFragment extends Fragment {

    FragmentSupportBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSupportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.whatsapp.setOnClickListener(v -> {
            String whatsappUrl = "https://wa.me/+923463962248";
            openUrl(whatsappUrl);
        });

        binding.linkedin.setOnClickListener(v -> {
            String linkedinUrl = "https://linkedin.com/in/kailash-kumar-78233a267";
            openUrl(linkedinUrl);
        });

        binding.github.setOnClickListener(v -> {
            String githubUrl = "https://github.com/Kumar91898";

            openUrl(githubUrl);
        });

        binding.resume.setOnClickListener(v -> {
            String portfolioUrl = "https://sites.google.com/view/kailash-kumar-91898/home?authuser=0";

            openUrl(portfolioUrl);
        });
    }

    private void openUrl(String url){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

            intent.setPackage("com.android.chrome");

            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}