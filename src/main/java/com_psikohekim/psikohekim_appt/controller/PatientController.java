package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.PatientRequest;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/addPatient")
    public PatientResponse addPatient(@RequestBody PatientRequest patient) throws ConflictException, InvalidRequestException {
        return patientService.addPatient(patient);
    }

    @GetMapping("/all")
    public Map<String, List<PatientResponse>> getPatients() throws ResourceNotFoundException {
        return patientService.getPatients();
    }

    @GetMapping ("/id")
    public PatientResponse getPatientById(@RequestParam Long id) throws ResourceNotFoundException {
        return patientService.getPatient(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidTherapistId(IllegalArgumentException ex) {
        return ex.getMessage();
    }

}
