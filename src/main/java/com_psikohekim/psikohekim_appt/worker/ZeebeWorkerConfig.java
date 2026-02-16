package com_psikohekim.psikohekim_appt.worker;

import com_psikohekim.psikohekim_appt.service.ProcessService;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Zeebe worker - create-assignment görevini işler.
 * Backend'e TherapistAssignment oluşturur.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ZeebeWorkerConfig {

    private final ZeebeClient zeebeClient;
    private final ProcessService processService;

    private JobWorker worker;

    @PostConstruct
    public void startWorker() {
        try {
            worker = zeebeClient.newWorker()
                    .jobType("create-assignment")
                    .handler(new CreateAssignmentHandler(processService))
                    .name("psikohekim-create-assignment")
                    .maxJobsActive(10)
                    .open();
            log.info("Zeebe worker 'create-assignment' başlatıldı");
        } catch (Exception e) {
            log.warn("Zeebe worker başlatılamadı (Zeebe çalışıyor olmalı): {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stopWorker() {
        if (worker != null) {
            worker.close();
            log.info("Zeebe worker kapatıldı");
        }
    }

    @RequiredArgsConstructor
    private static class CreateAssignmentHandler implements JobHandler {
        private final ProcessService processService;

        @Override
        public void handle(io.camunda.zeebe.client.api.worker.JobClient jobClient, io.camunda.zeebe.client.api.dto.ActivatedJob job) {
            try {
                Map<String, Object> variables = job.getVariablesAsMap();
                String processInstanceKey = String.valueOf(job.getProcessInstanceKey());

                Map<String, Object> request = new java.util.HashMap<>(Map.of(
                        "processInstanceKey", processInstanceKey,
                        "patientId", String.valueOf(variables.getOrDefault("patientId", "")),
                        "therapistId", String.valueOf(variables.getOrDefault("therapistId", "")),
                        "processName", String.valueOf(variables.getOrDefault("processName", "Randevu Onay Süreci")),
                        "description", String.valueOf(variables.getOrDefault("description", "")),
                        "startedBy", String.valueOf(variables.getOrDefault("startedBy", "Sistem")),
                        "sessionType", String.valueOf(variables.getOrDefault("sessionType", "INITIAL")),
                        "sessionFormat", String.valueOf(variables.getOrDefault("sessionFormat", "IN_PERSON"))
                ));
                Object scheduledDate = variables.get("scheduledDate");
                if (scheduledDate != null && !scheduledDate.toString().isBlank()) {
                    request.put("scheduledDate", scheduledDate);
                }

                processService.sendAssignmentRequest(request);

                jobClient.newCompleteCommand(job.getKey())
                        .variables(Map.of("processInstanceKey", processInstanceKey))
                        .send()
                        .join();

                log.info("Create-assignment tamamlandı: processInstanceKey={}", processInstanceKey);
            } catch (Exception e) {
                log.error("Create-assignment hatası: {}", e.getMessage());
                jobClient.newFailCommand(job.getKey())
                        .retries(job.getRetries() - 1)
                        .errorMessage(e.getMessage())
                        .send()
                        .join();
            }
        }
    }
}
