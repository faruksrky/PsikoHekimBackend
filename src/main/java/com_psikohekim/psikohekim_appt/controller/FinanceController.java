package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.response.FinanceMonthlySummaryResponse;
import com_psikohekim.psikohekim_appt.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
@Slf4j
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/monthly-summary")
    public ResponseEntity<FinanceMonthlySummaryResponse> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            FinanceMonthlySummaryResponse response = financeService.getMonthlySummary(year, month);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching finance monthly summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/mark-therapist-paid")
    public ResponseEntity<Void> markTherapistPaid(
            @RequestParam Long therapistId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            financeService.markTherapistPaid(therapistId, year, month);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking therapist paid: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
