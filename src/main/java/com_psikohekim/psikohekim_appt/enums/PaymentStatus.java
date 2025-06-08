package com_psikohekim.psikohekim_appt.enums;

public enum PaymentStatus {
    PENDING("Bekliyor"),
    PAID("Ödendi"),
    FAILED("Başarısız"),
    REFUNDED("İade Edildi"),
    CANCELLED("İptal Edildi"),
    PARTIAL("Kısmi Ödeme");
    
    private final String displayName;
    
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 