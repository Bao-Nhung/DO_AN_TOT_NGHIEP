package com.zestia.datn.zestia.service;

public final class CustomerPrivacy {
    private CustomerPrivacy() {
    }

    public static String maskName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Khách hàng Zestia";
        String normalized = fullName.trim().replaceAll("\\s+", " ");
        String[] parts = normalized.split(" ");
        if (parts.length == 1) {
            return parts[0].substring(0, 1) + "***";
        }
        String lastName = parts[parts.length - 1];
        return parts[0] + " " + lastName.substring(0, 1) + "***";
    }
}
