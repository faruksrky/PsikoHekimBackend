package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.service.BpmnServiceClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessService {

    private final BpmnServiceClient bpmnServiceClient;

    public ProcessService(BpmnServiceClient bpmnServiceClient) {
        this.bpmnServiceClient = bpmnServiceClient;
    }

    public Map<String, Object> startTherapistProcess(String businessKey) {
        Map<String, String> request = new HashMap<>();
        request.put("businessKey", businessKey);
        return bpmnServiceClient.startProcess(request);
    }
}
