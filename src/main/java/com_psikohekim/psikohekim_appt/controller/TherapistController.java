package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.response.AssignResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.service.TherapistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/therapist")
@RequiredArgsConstructor
public class TherapistController {

    private final TherapistService therapistService;

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

    @PostMapping("/{therapistId}/assign-patient/{patientId}")
    public ResponseEntity<AssignResponse> assignPatientToTherapist(
            @PathVariable Long therapistId,
            @PathVariable Long patientId) {
        try {
            AssignResponse response = therapistService.assignPatientToTherapist(therapistId, patientId);
            return ResponseEntity.ok(response); // Başarılı yanıt döner
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AssignResponse(false, e.getMessage())); // Hata durumunda
        }
    }


    @GetMapping("/by-email")
    public ResponseEntity<Long> getTherapistIdByEmail(@RequestParam String email) {
        Therapist therapist = therapistService.findByEmail(email);
        return ResponseEntity.ok(therapist.getTherapistId());
    }
}
