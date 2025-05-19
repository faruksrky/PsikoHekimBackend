package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Therapist;

import java.util.List;
import java.util.Map;

public interface TherapistService {
     TherapistResponse addTherapist(TherapistRequest psychologist) throws ConflictException, InvalidRequestException;
     Map<String, List<TherapistResponse>> getTherapists() throws ResourceNotFoundException;
     List<String> getPyschologistAreas();
     List<String> getPsychiatristAreas();
     Therapist findByEmail(String email);


     // Yeni eklenen metodlar
     Map<String, Object> sendAssignmentRequest(Map<String, Object> request) throws InvalidRequestException;
     Map<String, Object> acceptAssignmentRequest(Long requestId) throws InvalidRequestException;
     Map<String, Object> rejectAssignmentRequest(Long requestId) throws InvalidRequestException;
     List<Map<String, Object>> getPendingRequests(Long therapistId);
}