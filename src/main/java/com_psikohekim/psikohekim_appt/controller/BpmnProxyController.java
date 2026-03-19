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
    public ResponseEntity<?> startProcess(@RequestBody(required = false) Map<String, Object> request) {
        try {
            if (request == null || request.isEmpty()) {
                log.warn("BPMN start-process: boş istek");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "İstek gövdesi boş", "message", "variables alanı gereklidir"));
            }
            Object vars = request.get("variables");
            if (vars == null || !(vars instanceof Map)) {
                log.warn("BPMN start-process: variables eksik veya geçersiz");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "variables alanı eksik", "message", "patientId, therapistId vb. variables içinde olmalıdır"));
            }
            Object pid = ((Map<?, ?>) vars).get("patientId");
            Object tid = ((Map<?, ?>) vars).get("therapistId");
            log.info("BPMN start-process proxy: messageName={}, patientId={}, therapistId={}", request.get("messageName"), pid, tid);
            Map<String, Object> result = bpmnServiceClient.startProcess(request);
            return ResponseEntity.ok(result);
        } catch (FeignException e) {
            String bpmnResponse = e.contentUTF8();
            log.error("BPMN Feign hatası: status={}, body={}, message={}", e.status(), bpmnResponse, e.getMessage());
            String errorMsg = "BPMN servisi hata döndü";
            if (bpmnResponse != null && !bpmnResponse.isBlank()) {
                try {
                    if (bpmnResponse.contains("\"message\"")) {
                        int msgStart = bpmnResponse.indexOf("\"message\":\"") + 11;
                        int msgEnd = bpmnResponse.indexOf("\"", msgStart);
                        if (msgStart > 10 && msgEnd > msgStart) {
                            errorMsg = bpmnResponse.substring(msgStart, msgEnd).replace("\\\"", "\"");
                        }
                    } else if (bpmnResponse.contains("\"error\"")) {
                        int errStart = bpmnResponse.indexOf("\"error\":\"") + 9;
                        int errEnd = bpmnResponse.indexOf("\"", errStart);
                        if (errStart > 8 && errEnd > errStart) {
                            errorMsg = bpmnResponse.substring(errStart, errEnd).replace("\\\"", "\"");
                        }
                    }
                } catch (Exception ignored) {}
            }
            return ResponseEntity.status(e.status() >= 400 ? e.status() : 502)
                    .body(Map.of("error", errorMsg, "message", errorMsg, "bpmnResponse", bpmnResponse != null ? bpmnResponse : ""));
        } catch (Exception e) {
            log.error("BPMN proxy hatası: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage(), "message", e.getMessage(), "type", e.getClass().getSimpleName()));
        }
    }
}
