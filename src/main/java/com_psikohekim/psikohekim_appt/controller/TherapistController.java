package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.AssignTherapistRequest;
import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.service.PatientService;
import com_psikohekim.psikohekim_appt.service.TherapistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/therapist")
@RequiredArgsConstructor
public class TherapistController {

    private final TherapistService therapistService;
    private final PatientService patientService;

    @PostMapping("/addTherapist")
    public TherapistResponse addTherapist(@RequestBody TherapistRequest therapist) throws ConflictException, InvalidRequestException {
        return therapistService.addTherapist(therapist);
    }

    @GetMapping("/all")
    public Map<String, List<TherapistResponse>> getTherapists() throws ResourceNotFoundException {
        return therapistService.getTherapists();
    }

    @GetMapping("/psychiatry_areas")
    public List<String> getPsychiatryAreas() {
        return therapistService.getPsychiatristAreas();
    }

    @GetMapping("/psychologist_areas")
    public List<String> getPsychologistAreas() {
        return therapistService.getPyschologistAreas();
    }


    @GetMapping("/by-email")
    public ResponseEntity<Long> getTherapistIdByEmail(@RequestParam String email) {
        Therapist therapist = therapistService.findByEmail(email);
        return ResponseEntity.ok(therapist.getTherapistId());
    }
}
