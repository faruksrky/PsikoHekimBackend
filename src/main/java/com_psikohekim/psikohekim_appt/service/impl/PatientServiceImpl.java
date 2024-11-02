package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.PatientRequest;
import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.repository.PatientRepository;
import com_psikohekim.psikohekim_appt.service.PatientService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public void addPatient(PatientRequest patientRequest) {
        Patient patient = new Patient();
        patient.setFirstName(patientRequest.getFistName());
        patient.setLastName(patientRequest.getLastName());
        patient.setEmail(patientRequest.getEmail());
        patient.setPhoneNumber(patientRequest.getPhoneNumber());
        patient.setAddress(patientRequest.getAddress());
        patient.setDateOfBirth(patientRequest.getDateOfBirth());
        patient.setCountry(patientRequest.getCountry());
        patient.setGender(patientRequest.getGender());
        try {
            patientRepository.save(patient);
        } catch (Exception e) {
            throw new RuntimeException("Hasta Kayit edilemedi!", e);
        }
    }
}