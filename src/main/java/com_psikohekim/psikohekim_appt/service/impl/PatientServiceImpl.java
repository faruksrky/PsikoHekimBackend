package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.PatientRequest;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.repository.PatientRepository;
import com_psikohekim.psikohekim_appt.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;

    @Override
    public PatientResponse addPatient(PatientRequest patientRequest) {
        Patient patient = modelMapper.map(patientRequest, Patient.class);
        patientRepository.save(patient);
        return modelMapper.map(patient, PatientResponse.class);
    }

    @Override
    public Map<String, List<PatientResponse>> getPatients() throws ResourceNotFoundException {
        List<Patient> patients = patientRepository.findAll();
        if (patients.isEmpty()) {
            throw new ResourceNotFoundException("Danışan bulunamadı!");
        }

        List<PatientResponse> patientResponses = patients.stream()
                .map(patient -> modelMapper.map(patient, PatientResponse.class))
                .collect(Collectors.toList());
        Map<String, List<PatientResponse>> response = new HashMap<>();
        response.put("patients", patientResponses);
        return response;
    }

    public PatientResponse getPatient(Long patientId) throws ResourceNotFoundException {
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

            return modelMapper.map(patient, PatientResponse.class);
        } catch (Exception ex) {
            throw new RuntimeException("Error getting patient: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<PatientResponse> getPatientsByIds(List<Long> patientIds) {
        try {
            // String ID'leri Long'a çevir
            List<Long> ids = patientIds.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            // Repository'den hastaları getir
            List<Patient> patients = patientRepository.findAllById(ids);

            // Patient entity'lerini PatientResponse DTO'larına dönüştür
            return patients.stream()
                    .map(patient -> PatientResponse.builder()
                            .patientId(patient.getPatientId())
                            .patientFirstName(patient.getPatientFirstName())
                            .patientLastName(patient.getPatientLastName())
                            .patientAge(patient.getPatientAge())
                            .patientEmail(patient.getPatientEmail())
                            .patientPhoneNumber(patient.getPatientPhoneNumber())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Hasta bilgileri alınırken bir hata oluştu", e);
        }
    }

}

