package com_psikohekim.psikohekim_appt.enums;

public enum AssignmentStatus {
    ACTIVE("Aktif"),
    COMPLETED("Tamamlandı"),
    PAUSED("Durduruldu"),
    CANCELLED("İptal Edildi"),
    ON_HOLD("Beklemede");
    
    private final String displayName;
    
    AssignmentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 