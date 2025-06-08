package com_psikohekim.psikohekim_appt.enums;

public enum PaymentMethod {
    CASH("Nakit"),
    CREDIT_CARD("Kredi Kartı"),
    BANK_TRANSFER("Banka Havalesi"),
    ONLINE("Online Ödeme"),
    CHECK("Çek");
    
    private final String displayName;
    
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 