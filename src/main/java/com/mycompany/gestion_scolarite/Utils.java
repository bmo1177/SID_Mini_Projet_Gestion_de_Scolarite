/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestion_scolarite;

import java.sql.*;
import java.time.LocalDate;

public class Utils {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/gestion_scolarite2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";
    
    /**
     * Get database connection - reuse existing connection logic
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Validate grade is between 0 and 20
     */
    public static boolean validateGrade(double grade) {
        return grade >= 0 && grade <= 20;
    }
    
    /**
     * Get current academic year (2025-2026)
     */
    public static String getCurrentAcademicYear() {
        int currentYear = LocalDate.now().getYear();
        // For the project, we use 2025-2026 as specified
        return "2025-2026";
    }
    
    /**
     * Simple logging for audit purposes
     */
    public static void logActivity(String userId, String action, String details) {
        // In a real system, this would write to database
        System.out.println("[" + java.time.LocalDateTime.now() + "] User " + userId + ": " + action + " - " + details);
    }
    
    /**
     * Format double to 2 decimal places
     */
    public static String formatDouble(double value) {
        return String.format("%.2f", value);
    }
}