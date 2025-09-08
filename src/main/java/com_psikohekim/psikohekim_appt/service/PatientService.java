package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.request.PatientRequest;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Map;

public interface PatientService {

    PatientResponse addPatient(PatientRequest patient)  throws ConflictException, InvalidRequestException;
    Map<String, List<PatientResponse>> getPatients() throws ResourceNotFoundException;
    PatientResponse getPatient(Long patientId) throws ResourceNotFoundException;
    List<PatientResponse> getPatientsByIds(List<Long> patientIds);
}
