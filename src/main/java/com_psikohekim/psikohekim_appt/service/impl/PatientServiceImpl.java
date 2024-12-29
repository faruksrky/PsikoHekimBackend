package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.PatientRequest;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.repository.PatientRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
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
    private final TherapistRepository therapistRepository;
    private final ModelMapper modelMapper;

    @Override
    public PatientResponse addPatient(PatientRequest patientRequest) {
        Patient patient = modelMapper.map(patientRequest, Patient.class);
        patientRepository.save(patient);

        // Terapist ile ilişkilendir
        if (patientRequest.getTherapistId() != null) {
            Therapist therapist = therapistRepository.findById(patientRequest.getTherapistId())
                    .orElseThrow(() -> new IllegalArgumentException("Danışman bulunamadı!"));
            therapist.getPatients().add(patient);
            patient.getTherapists().add(therapist);
            therapistRepository.save(therapist);
        }

        return modelMapper.map(patient, PatientResponse.class);
    }

    @Override
    public Map<String, List<PatientResponse>> getPatients() throws ResourceNotFoundException {
        List<Patient> patients = patientRepository.findAll();
        if (patients.isEmpty()) {
            throw new ResourceNotFoundException("Hasta bulunamadı!");
        }

        List<PatientResponse> patientResponses = patients.stream()
                .map(patient -> modelMapper.map(patient, PatientResponse.class))
                .collect(Collectors.toList());
        Map<String, List<PatientResponse>> response = new HashMap<>();
        response.put("patients", patientResponses);
        return response;
    }




}

