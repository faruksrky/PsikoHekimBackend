package com_psikohekim.psikohekim_appt.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * Danışman (therapist) aylık hakediş özeti - finans sekmesi için
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TherapistMonthlyExpenseResponse {

    private Long therapistId;
    private String therapistName;
    private int sessionCount;
    private BigDecimal totalEarning;      // Toplam hakediş
    private BigDecimal paidAmount;       // Ödenen tutar
    private BigDecimal pendingAmount;   // Bekleyen ödeme
    private String status;               // FULLY_PAID, PARTIAL, PENDING
}
