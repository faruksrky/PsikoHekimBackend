package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.PatientRequest;
import com_psikohekim.psikohekim_appt.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController("patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    private void addPatient(PatientRequest patient) {
        patientService.addPatient(patient);
    }

}
