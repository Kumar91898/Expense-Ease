package com.kumar.expenseease.dataModels;

public class budgetClass {
    private String category;
    private double totalBudget;
    private double spentAmount;
    private String imageUrl;

    public budgetClass(String category, double totalBudget, double spentAmount, String imageUrl) {
        this.category = category;
        this.totalBudget = totalBudget;
        this.spentAmount = spentAmount;
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
