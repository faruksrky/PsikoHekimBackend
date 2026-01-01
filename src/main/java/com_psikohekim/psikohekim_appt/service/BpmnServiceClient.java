package com_psikohekim.psikohekim_appt.service;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "bpmn-service", url = "${services.bpmn.url:http://localhost:8082}")
public interface BpmnServiceClient {

    @PostMapping("/api/bpmn/patient/start-process")
    Map<String, Object> startProcess(@RequestBody Map<String, String> request);

    @GetMapping("/api/bpmn/tasks")
    List<Map<String, Object>> getTasks(@RequestParam("processInstanceId") String processInstanceId);

    @PostMapping("/api/bpmn/message")
    Map<String, Object> publishMessage(@RequestBody Map<String, Object> request);

    @GetMapping("/api/bpmn/process/{processInstanceKey}")
    Map<String, Object> getProcessInfo(@PathVariable String processInstanceKey);
}


