package com_psikohekim.psikohekim_appt.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionCompletionRequest {
    
    @NotNull(message = "Seans notları gereklidir")
    @Size(min = 10, max = 2000, message = "Seans notları 10-2000 karakter arasında olmalıdır")
    private String sessionNotes;
    
    @Size(max = 1000, message = "Tedavi planı 1000 karakterden fazla olamaz")
    private String treatmentPlan;
    
    @Size(max = 500, message = "Değerlendirme notları 500 karakterden fazla olamaz")
    private String assessmentNotes;
    
    @NotNull(message = "Seans ücreti gereklidir")
    @DecimalMin(value = "0.0", inclusive = false, message = "Seans ücreti 0'dan büyük olmalıdır")
    private BigDecimal sessionFee;
    
    private String nextSessionDate; // ISO format: "2024-01-15T14:30:00"
    
    @Size(max = 500, message = "Hasta durumu notu 500 karakterden fazla olamaz")
    private String patientCondition;
    
    @Size(max = 300, message = "Ev ödevi 300 karakterden fazla olamaz")
    private String homework;
    
    private Boolean scheduleNextSession = false;
    
    // Ödeme bilgileri
    private String paymentMethod; // CASH, CARD, TRANSFER
    private Boolean paymentReceived = false;
    private String paymentNotes;
} 