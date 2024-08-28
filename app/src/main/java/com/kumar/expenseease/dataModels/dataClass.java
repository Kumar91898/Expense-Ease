package com.kumar.expenseease.dataModels;

import java.util.Date;

public class dataClass {
    private String title;
    private String date;
    private String imageUrl;
    private String category;
    private String documentId;
    private double amount;
    private boolean isIncome;

    public dataClass(String title, String date, String imageUrl, String category, String documentId, double amount, boolean isIncome) {
        this.title = title;
        this.date = date;
        this.imageUrl = imageUrl;
        this.category = category;
        this.documentId = documentId;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getCategory() {
        return category;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }
}
