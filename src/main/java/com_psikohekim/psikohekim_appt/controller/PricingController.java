package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.response.ClientSessionPriceResponse;
import com_psikohekim.psikohekim_appt.dto.response.ConsultantEarningResponse;
import com_psikohekim.psikohekim_appt.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pricing")
public class PricingController {

    private final PricingService pricingService;

    @GetMapping("/client/sessions/{sessionId}/price")
    public ResponseEntity<ClientSessionPriceResponse> getClientPrice(@PathVariable Long sessionId) {
        ClientSessionPriceResponse response = pricingService.getClientSessionPrice(sessionId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/consultant/sessions/{sessionId}/earning")
    public ResponseEntity<ConsultantEarningResponse> getConsultantEarning(@PathVariable Long sessionId) {
        ConsultantEarningResponse response = pricingService.getConsultantEarning(sessionId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
