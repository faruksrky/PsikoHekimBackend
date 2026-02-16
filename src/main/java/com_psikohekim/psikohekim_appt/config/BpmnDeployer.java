package com_psikohekim.psikohekim_appt.config;

import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Backend başlarken BPMN sürecini Zeebe'ye deploy eder.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BpmnDeployer {

    private final ZeebeClient zeebeClient;

    @PostConstruct
    public void deployBpmn() {
        try {
            ClassPathResource resource = new ClassPathResource("bpmn/therapist-assignment.bpmn");
            try (InputStream is = resource.getInputStream()) {
                zeebeClient.newDeployResourceCommand()
                        .addResourceStream(is, "therapist-assignment.bpmn")
                        .send()
                        .join();
                log.info("BPMN süreci Zeebe'ye deploy edildi: therapist-assignment.bpmn");
            }
        } catch (Exception e) {
            log.warn("BPMN deploy atlandı (Zeebe bağlantısı yok olabilir): {}", e.getMessage());
        }
    }
}
