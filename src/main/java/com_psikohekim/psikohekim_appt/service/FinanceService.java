package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.response.FinanceMonthlySummaryResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistMonthlyExpenseResponse;

public interface FinanceService {

    /**
     * Aylık finans özeti - toplam + danışman başına ödenen/bekleyen
     * @param therapistId Opsiyonel - verilirse sadece o danışmanın seansları (danışman kendi finansı için)
     */
    FinanceMonthlySummaryResponse getMonthlySummary(int year, int month, Long therapistId);

    /**
     * Danışmanın ay içi hakedişlerini ödendi olarak işaretle
     */
    void markTherapistPaid(Long therapistId, int year, int month);
}
