package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.service.BpmnServiceClient;
import feign.FeignException;
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
            Object vars = request.get("variables");
            Object pid = vars instanceof Map ? ((Map<?, ?>) vars).get("patientId") : null;
            Object tid = vars instanceof Map ? ((Map<?, ?>) vars).get("therapistId") : null;
            log.info("BPMN start-process proxy: messageName={}, patientId={}, therapistId={}", request.get("messageName"), pid, tid);
            Map<String, Object> result = bpmnServiceClient.startProcess(request);
            return ResponseEntity.ok(result);
        } catch (FeignException e) {
            String bpmnResponse = e.contentUTF8();
            log.error("BPMN Feign hatası: status={}, body={}, message={}", e.status(), bpmnResponse, e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", e.getMessage(), "bpmnResponse", bpmnResponse != null ? bpmnResponse : ""));
        } catch (Exception e) {
            log.error("BPMN proxy hatası: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage(), "type", e.getClass().getSimpleName()));
        }
    }
}
