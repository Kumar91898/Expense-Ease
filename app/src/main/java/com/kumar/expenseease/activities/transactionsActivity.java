package com.kumar.expenseease.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentPagerAdapter;

import com.kumar.expenseease.R;
import com.kumar.expenseease.adapters.VRAdapter;
import com.kumar.expenseease.databinding.ActivityTransactionsBinding;
import com.kumar.expenseease.fragments.allFragment;
import com.kumar.expenseease.fragments.expenseFragment;
import com.kumar.expenseease.fragments.incomeFragment;

public class transactionsActivity extends AppCompatActivity {

    ActivityTransactionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tabLayout.setupWithViewPager(binding.viewPagerTransaction);

        VRAdapter vrAdapter = new VRAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vrAdapter.addFragments(new allFragment(), "All");
        vrAdapter.addFragments(new incomeFragment(), "Income");
        vrAdapter.addFragments(new expenseFragment(), "Expense");

        binding.viewPagerTransaction.setAdapter(vrAdapter);
    }
}