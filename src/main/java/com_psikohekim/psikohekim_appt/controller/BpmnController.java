package com_psikohekim.psikohekim_appt.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * BPMN REST API - Zeebe mesaj yayınlama ve süreç başlatma.
 * Frontend /api/bpmn/patient/start-process endpoint'ini çağırır.
 */
@RestController
@RequestMapping("/api/bpmn")
@RequiredArgsConstructor
@Slf4j
public class BpmnController {

    private final ZeebeClient zeebeClient;

    @PostMapping("/patient/start-process")
    public ResponseEntity<Map<String, Object>> startProcess(@RequestBody Map<String, Object> request) {
        String messageName = (String) request.get("messageName");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");

        if (messageName == null || messageName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "messageName gerekli"));
        }
        if (variables == null) {
            variables = Map.of();
        }

        // Correlation key: mesajın hangi süreç instance'ına gideceğini belirler
        String correlationKey = buildCorrelationKey(variables);
        String messageId = UUID.randomUUID().toString();

        try {
            PublishMessageResponse response = zeebeClient.newPublishMessageCommand()
                    .messageName(messageName)
                    .messageId(messageId)
                    .correlationKey(correlationKey)
                    .variables(variables)
                    .send()
                    .join();

            log.info("BPMN mesajı yayınlandı: messageName={}, correlationKey={}", messageName, correlationKey);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "messageName", messageName,
                    "correlationKey", correlationKey,
                    "messageId", messageId
            ));
        } catch (Exception e) {
            log.error("BPMN mesaj yayınlama hatası: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "BPMN süreci başlatılamadı: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    /**
     * Mesaj yayınlama - therapist_decision vb.
     * ProcessServiceImpl BpmnServiceClient üzerinden bu endpoint'i çağırır.
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> publishMessage(@RequestBody Map<String, Object> request) {
        String messageName = (String) request.get("messageName");
        String correlationKey = (String) request.get("correlationKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");

        if (messageName == null || messageName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "messageName gerekli"));
        }
        if (correlationKey == null || correlationKey.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "correlationKey gerekli"));
        }
        if (variables == null) {
            variables = Map.of();
        }

        String messageId = UUID.randomUUID().toString();

        try {
            zeebeClient.newPublishMessageCommand()
                    .messageName(messageName)
                    .messageId(messageId)
                    .correlationKey(correlationKey)
                    .variables(variables)
                    .send()
                    .join();

            log.info("BPMN mesajı yayınlandı: messageName={}, correlationKey={}", messageName, correlationKey);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "messageName", messageName,
                    "correlationKey", correlationKey
            ));
        } catch (Exception e) {
            log.error("BPMN mesaj yayınlama hatası: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Mesaj yayınlanamadı: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    private String buildCorrelationKey(Map<String, Object> variables) {
        Object patientId = variables.get("patientId");
        Object therapistId = variables.get("therapistId");
        if (patientId != null && therapistId != null) {
            return patientId + "-" + therapistId + "-" + System.currentTimeMillis();
        }
        return UUID.randomUUID().toString();
    }
}
