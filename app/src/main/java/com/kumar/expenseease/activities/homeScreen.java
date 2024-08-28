package com.kumar.expenseease.activities;

import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kumar.expenseease.R;
import com.kumar.expenseease.databinding.ActivityHomeScreenBinding;
import com.kumar.expenseease.fragments.accountFragment;
import com.kumar.expenseease.fragments.budgetFragment;
import com.kumar.expenseease.fragments.homeFragment;
import com.kumar.expenseease.fragments.walletFragment;
import com.kumar.expenseease.utils.NetworkUtil;

public class homeScreen extends AppCompatActivity {
    ActivityHomeScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for internet connection
        if (!NetworkUtil.isNetworkConnected(this)) {
            showNoInternetDialog();
        } else {
            // Initialize your app normally
            binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            replaceFragment(new homeFragment());

            binding.bottomNavigationView.setOnItemSelectedListener(menuItem -> {
                resetIcons(binding.bottomNavigationView);
                int itemId = menuItem.getItemId();
                if (itemId == R.id.home) {
                    menuItem.setIcon(R.drawable.baseline_home_24_filled);
                    replaceFragment(new homeFragment());
                } else if (itemId == R.id.wallet) {
                    menuItem.setIcon(R.drawable.baseline_account_balance_wallet_24_filled);
                    replaceFragment(new walletFragment());
                } else if (itemId == R.id.budget) {
                    menuItem.setIcon(R.drawable.outline_attach_money_24);
                    replaceFragment(new budgetFragment());
                } else if (itemId == R.id.account) {
                    menuItem.setIcon(R.drawable.baseline_account_box_24_filled);
                    replaceFragment(new accountFragment());
                }
                return true;
            });
        }
    }

    private void resetIcons(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu().findItem(R.id.home).setIcon(R.drawable.outline_home_24);
        bottomNavigationView.getMenu().findItem(R.id.wallet).setIcon(R.drawable.outline_account_balance_wallet_24);
        bottomNavigationView.getMenu().findItem(R.id.budget).setIcon(R.drawable.outline_attach_money_24);
        bottomNavigationView.getMenu().findItem(R.id.account).setIcon(R.drawable.outline_account_box_24);
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
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
