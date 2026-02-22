package com_psikohekim.psikohekim_appt.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Finans aylık özet - toplam + danışman başına
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceMonthlySummaryResponse {

    private int year;
    private int month;

    // Toplam özet
    private BigDecimal totalIncome;      // Toplam ciro (seans ücretleri)
    private BigDecimal totalExpense;     // Toplam hakediş (danışman ödemeleri)
    private BigDecimal totalPaid;       // Ödenen toplam
    private BigDecimal totalPending;    // Bekleyen toplam
    private BigDecimal totalProfit;     // Kar (ciro - gider)

    private List<TherapistMonthlyExpenseResponse> therapistExpenses;
    private List<SessionResponse> sessions;  // Gelir tablosu için seans listesi
}
