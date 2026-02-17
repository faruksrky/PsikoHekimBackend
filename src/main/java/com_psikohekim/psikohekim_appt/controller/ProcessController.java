package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.PendingRequest;
import com_psikohekim.psikohekim_appt.dto.request.ProcessActionRequest;
import com_psikohekim.psikohekim_appt.dto.request.PublishMessageRequest;
import com_psikohekim.psikohekim_appt.dto.response.AssignmentResponse;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startProcess(@RequestParam String businessKey) throws InvalidRequestException {
        processService.startTherapistProcess(businessKey);
        return ResponseEntity.ok("Process started with businessKey: " + businessKey);
    }

    @PostMapping("/send-assignment-request")
    public
    ResponseEntity<AssignmentResponse> sendAssignmentRequest(@RequestBody Map<String, Object> request) throws InvalidRequestException {
        return ResponseEntity.ok(processService.sendAssignmentRequest(request));
    }

    /**
     * Admin: therapistId verilmezse tüm bekleyen atamalar.
     * Terapist: therapistId ile sadece kendi bekleyen atamaları.
     */
    @GetMapping("/inbox/pending")
    public ResponseEntity<List<PendingRequest>> getPendingRequests(
            @RequestParam(required = false) Long therapistId) throws InvalidRequestException {
        if (therapistId == null) {
            return ResponseEntity.ok(processService.getIncompleteAssignments());
        }
        return ResponseEntity.ok(processService.getPendingRequests(therapistId));
    }


    @PostMapping("/inbox/action")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @RequestBody ProcessActionRequest request) throws InvalidRequestException {

        if (request.getProcessInstanceKey() == null) {
            throw new InvalidRequestException("ProcessInstanceKey cannot be null","ProcessInstanceKey cannot be null\"");
        }

        try {
            Map<String, Object> result = processService.updateAssignmentStatus(
                    request.getProcessInstanceKey().toString(),
                    request.getAction()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new InvalidRequestException("Error updating status: " + e.getMessage(),"Error updating status: " + e.getMessage());
        }
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> publishMessage(@RequestBody PublishMessageRequest request) {
        return ResponseEntity.ok(processService.publishMessage(request));
    }

    @GetMapping("/incomplete-assignments")
    public ResponseEntity<List<PendingRequest>> getIncompleteAssignments() throws InvalidRequestException {
        return ResponseEntity.ok(processService.getIncompleteAssignments());
    }

    @GetMapping("/status/{processInstanceKey}")
    public ResponseEntity<Map<String, Object>> getProcessStatus(@PathVariable String processInstanceKey) throws InvalidRequestException {
        return ResponseEntity.ok(processService.getProcessStatus(processInstanceKey));
    }

    @PostMapping("/restart-assignment/{assignmentId}")
    public ResponseEntity<Map<String, Object>> restartAssignment(@PathVariable Long assignmentId) throws InvalidRequestException {
        return ResponseEntity.ok(processService.restartAssignment(assignmentId));
    }
}
