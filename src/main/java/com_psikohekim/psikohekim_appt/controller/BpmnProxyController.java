package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.service.BpmnServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * BPMN API proxy - Mimari: Frontend → Backend (JWT) → BPMN (Feign)
 * BPMN internal servistir, frontend doğrudan erişmez.
 */
@Slf4j
@RestController
@RequestMapping("/api/bpmn/patient")
@RequiredArgsConstructor
public class BpmnProxyController {

    private final BpmnServiceClient bpmnServiceClient;

    @PostMapping("/start-process")
    public ResponseEntity<?> startProcess(@RequestBody Map<String, Object> request) {
        try {
            log.info("BPMN start-process proxy: messageName={}", request.get("messageName"));
            Map<String, Object> result = bpmnServiceClient.startProcess(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("BPMN proxy hatası: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
