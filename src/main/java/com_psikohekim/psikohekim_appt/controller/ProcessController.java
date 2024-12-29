package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.service.impl.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/process")
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startProcess(@RequestParam String businessKey) {
        processService.startTherapistProcess(businessKey);
        return ResponseEntity.ok("Process started with businessKey: " + businessKey);
    }
}