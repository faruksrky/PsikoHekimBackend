package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.request.SessionCompletionRequest;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistDashboardResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientSummaryDto;
import com_psikohekim.psikohekim_appt.dto.response.TherapistStatistics;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Therapist;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TherapistService {
     // Mevcut metodlar
     TherapistResponse addTherapist(TherapistRequest psychologist) throws ConflictException, InvalidRequestException;
     Map<String, List<TherapistResponse>> getTherapists() throws ResourceNotFoundException;
     List<String> getPyschologistAreas();
     List<String> getPsychiatristAreas();
     Therapist findByEmail(String email);
     
     // Yeni dashboard metodları
     TherapistDashboardResponse getTherapistDashboard(Long therapistId) throws ResourceNotFoundException;
     List<PatientSummaryDto> getTherapistPatients(Long therapistId, int page, int size) throws ResourceNotFoundException;
     TherapistStatistics getTherapistStatistics(Long therapistId, String period) throws ResourceNotFoundException;
     
     // Seans yönetimi
     void completeSession(Long therapistId, Long appointmentId, SessionCompletionRequest request) throws ResourceNotFoundException;
     
     // Hasta yönetimi
     PatientSummaryDto getPatientSummary(Long therapistId, Long patientId) throws ResourceNotFoundException;
}