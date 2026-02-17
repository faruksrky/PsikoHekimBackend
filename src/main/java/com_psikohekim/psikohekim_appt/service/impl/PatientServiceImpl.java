package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.PatientRequest;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.model.TherapistPatient;
import com_psikohekim.psikohekim_appt.enums.AssignmentStatus;
import com_psikohekim.psikohekim_appt.model.TherapistAssignment;
import com_psikohekim.psikohekim_appt.repository.PatientRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistAssignmentRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistPatientRepository;
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
    private final TherapistPatientRepository therapistPatientRepository;
    private final TherapistAssignmentRepository therapistAssignmentRepository;
    private final ModelMapper modelMapper;

    @Override
    public PatientResponse addPatient(PatientRequest patientRequest) {
        Patient patient = modelMapper.map(patientRequest, Patient.class);
        patientRepository.save(patient);
        return modelMapper.map(patient, PatientResponse.class);
    }

    @Override
    public PatientResponse updatePatient(Long patientId, PatientRequest patientRequest) throws ResourceNotFoundException {
        // Önce mevcut patient'ı bul
        Patient existingPatient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));
        
        // Yeni bilgileri güncelle
        existingPatient.setPatientFirstName(patientRequest.getPatientFirstName());
        existingPatient.setPatientLastName(patientRequest.getPatientLastName());
        existingPatient.setPatientAge(patientRequest.getPatientAge());
        existingPatient.setPatientEmail(patientRequest.getPatientEmail());
        existingPatient.setPatientPhoneNumber(patientRequest.getPatientPhoneNumber());
        existingPatient.setPatientGender(patientRequest.getPatientGender());
        existingPatient.setPatientCountry(patientRequest.getPatientCountry());
        existingPatient.setPatientCity(patientRequest.getPatientCity());
        existingPatient.setPatientAddress(patientRequest.getPatientAddress());
        existingPatient.setPatientReference(patientRequest.getPatientReference());
        
        // Güncellenmiş patient'ı kaydet
        Patient updatedPatient = patientRepository.save(existingPatient);
        return modelMapper.map(updatedPatient, PatientResponse.class);
    }

    @Override
    public Map<String, List<PatientResponse>> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        if (patients.isEmpty()) {
            Map<String, List<PatientResponse>> empty = new HashMap<>();
            empty.put("patients", List.of());
            return empty;
        }

        List<PatientResponse> patientResponses = patients.stream()
                .map(patient -> {
                    PatientResponse patientResponse = modelMapper.map(patient, PatientResponse.class);

                    applyAssignmentStatus(patient, patientResponse);
                    
                    return patientResponse;
                })
                .collect(Collectors.toList());
                
        Map<String, List<PatientResponse>> response = new HashMap<>();
        response.put("patients", patientResponses);
        return response;
    }

    public PatientResponse getPatient(Long patientId) throws ResourceNotFoundException {
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

            PatientResponse response = modelMapper.map(patient, PatientResponse.class);
            applyAssignmentStatus(patient, response);
            return response;
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
                            .assignmentStatus(determineAssignmentStatus(patient))
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Hasta bilgileri alınırken bir hata oluştu", e);
        }
    }

    private void applyAssignmentStatus(Patient patient, PatientResponse response) {
        List<TherapistPatient> assignments = therapistPatientRepository.findByPatientAndAssignmentStatus(
                patient, AssignmentStatus.ACTIVE);

        if (!assignments.isEmpty()) {
            TherapistPatient activeAssignment = assignments.get(0);
            response.setTherapistId(activeAssignment.getTherapist().getTherapistId());
            response.setAssignmentStatus("ASSIGNED");
            return;
        }

        boolean hasPending = therapistAssignmentRepository.existsByPatientIdAndStatus(
                String.valueOf(patient.getPatientId()),
                TherapistAssignment.AssignmentStatus.PENDING
        );
        response.setTherapistId(null);
        response.setAssignmentStatus(hasPending ? "PENDING" : "UNASSIGNED");
    }

    private String determineAssignmentStatus(Patient patient) {
        List<TherapistPatient> assignments = therapistPatientRepository.findByPatientAndAssignmentStatus(
                patient, AssignmentStatus.ACTIVE);
        if (!assignments.isEmpty()) {
            return "ASSIGNED";
        }
        boolean hasPending = therapistAssignmentRepository.existsByPatientIdAndStatus(
                String.valueOf(patient.getPatientId()),
                TherapistAssignment.AssignmentStatus.PENDING
        );
        return hasPending ? "PENDING" : "UNASSIGNED";
    }

}

