package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.PatientRequest;
import org.springframework.stereotype.Service;

@Service
public interface PatientService {

    public void addPatient(PatientRequest patient);

}
