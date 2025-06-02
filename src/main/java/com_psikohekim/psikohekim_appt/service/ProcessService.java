package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.request.PendingRequest;
import com_psikohekim.psikohekim_appt.dto.request.PublishMessageRequest;
import com_psikohekim.psikohekim_appt.dto.response.AssignmentResponse;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;

import java.util.List;
import java.util.Map;

public interface ProcessService {

    Map<String, Object> startTherapistProcess(String businessKey) throws InvalidRequestException;

    AssignmentResponse sendAssignmentRequest(Map<String, Object> request) throws InvalidRequestException;

    List<PendingRequest> getPendingRequests(Long therapistId) throws InvalidRequestException;

    Map<String, Object> updateAssignmentStatus(String processInstanceKey, String action) throws InvalidRequestException;

    Map<String, Object> publishMessage(PublishMessageRequest request);

}
