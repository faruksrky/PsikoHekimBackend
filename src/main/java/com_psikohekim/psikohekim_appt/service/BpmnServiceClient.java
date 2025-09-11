package com_psikohekim.psikohekim_appt.service;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "bpmn-service", url = "${services.bpmn.url:http://localhost:8082}")
public interface BpmnServiceClient {

    @PostMapping("/start-process")
    Map<String, Object> startProcess(@RequestBody Map<String, String> request);

    @GetMapping("/tasks")
    List<Map<String, Object>> getTasks(@RequestParam("processInstanceId") String processInstanceId);

    @PostMapping("/message")
    Map<String, Object> publishMessage(@RequestBody Map<String, Object> request);
}


