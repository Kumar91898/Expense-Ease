package com.kumar.expenseease.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static Date convertStringToDate(String dateString) {
        // Define the date format
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); // Change the pattern according to your date format

        try {
            // Parse the date string to a Date object
            return formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}

