package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.BpmnAssignmentRequest;
import com_psikohekim.psikohekim_appt.dto.request.SessionCompletionRequest;
import com_psikohekim.psikohekim_appt.dto.response.ApiResponse;
import com_psikohekim.psikohekim_appt.dto.response.AssignResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientSummaryDto;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.service.TherapistPatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Therapist-Patient Assignment Management Controller
 * Yaklaşım 2: Ayrı DTO'lar ile temiz mimari
 */
@Slf4j
@RestController
@RequestMapping("/therapist-patient")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TherapistPatientController {

    private final TherapistPatientService therapistPatientService;

    // ========== ASSIGNMENT CREATION ENDPOINTS ==========

    /**
     * BPMN sürecinden gelen assignment request'i işleme
     * Endpoint: POST /api/therapist-patient/assign-from-bpmn
     */
    @PostMapping("/assign-from-bpmn")
    public ResponseEntity<ApiResponse<AssignResponse>> processAssignmentFromBpmn(
            @Valid @RequestBody BpmnAssignmentRequest bpmnRequest) {
        try {
            log.info("BPMN assignment request alındı: Process={}, Patient={}, Therapist={}",
                    bpmnRequest.getProcessId(), bpmnRequest.getPatientId(), bpmnRequest.getTherapistId());

            AssignResponse response = therapistPatientService.processAssignmentFromBpmn(bpmnRequest);

            return ResponseEntity.ok(ApiResponse.success(response, "BPMN assignment başarıyla işlendi"));

        } catch (ResourceNotFoundException e) {
            log.error("BPMN assignment hatası: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "BPMN assignment işlenemedi"));
        } catch (Exception e) {
            log.error("BPMN assignment beklenmeyen hata: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Sistem hatası", "BPMN assignment işlenemedi"));
        }
    }

    // ========== ASSIGNMENT MANAGEMENT ENDPOINTS ==========

    /**
     * Assignment iptal etme
     * Endpoint: DELETE /api/therapist-patient/{therapistId}/patients/{patientId}
     */
    @DeleteMapping("/{therapistId}/patients/{patientId}")
    public ResponseEntity<ApiResponse<String>> unassignTherapistFromPatient(
            @PathVariable Long therapistId,
            @PathVariable Long patientId) {
        try {
            log.info("Assignment iptal ediliyor: Therapist={}, Patient={}", therapistId, patientId);

            therapistPatientService.unassignTherapistFromPatient(therapistId, patientId);

            return ResponseEntity.ok(ApiResponse.success("OK", "Assignment başarıyla iptal edildi"));

        } catch (ResourceNotFoundException e) {
            log.error("Assignment iptal hatası: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Assignment iptal beklenmeyen hata: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Sistem hatası", "Assignment iptal edilemedi"));
        }
    }

    /**
     * Assignment durumu güncelleme
     * Endpoint: PUT /api/therapist-patient/{therapistId}/patients/{patientId}/status
     */
    @PutMapping("/{therapistId}/patients/{patientId}/status")
    public ResponseEntity<ApiResponse<String>> updateAssignmentStatus(
            @PathVariable Long therapistId,
            @PathVariable Long patientId,
            @RequestParam String status) {
        try {
            log.info("Assignment durumu güncelleniyor: Therapist={}, Patient={}, Status={}",
                    therapistId, patientId, status);

            therapistPatientService.updateAssignmentStatus(therapistId, patientId, status);

            return ResponseEntity.ok(ApiResponse.success("OK", "Assignment durumu güncellendi"));

        } catch (ResourceNotFoundException e) {
            log.error("Assignment durum güncelleme hatası: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Assignment durum güncelleme beklenmeyen hata: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Sistem hatası", "Assignment durumu güncellenemedi"));
        }
    }

    /**
     * Assignment öncelik güncelleme
     * Endpoint: PUT /api/therapist-patient/{therapistId}/patients/{patientId}/priority
     */
    @PutMapping("/{therapistId}/patients/{patientId}/priority")
    public ResponseEntity<ApiResponse<String>> updateAssignmentPriority(
            @PathVariable Long therapistId,
            @PathVariable Long patientId,
            @RequestParam Integer priority) {
        try {
            log.info("Assignment önceliği güncelleniyor: Therapist={}, Patient={}, Priority={}",
                    therapistId, patientId, priority);

            therapistPatientService.updateAssignmentPriority(therapistId, patientId, priority);

            return ResponseEntity.ok(ApiResponse.success("OK", "Assignment önceliği güncellendi"));

        } catch (ResourceNotFoundException e) {
            log.error("Assignment öncelik güncelleme hatası: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Assignment öncelik güncelleme beklenmeyen hata: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Sistem hatası", "Assignment önceliği güncellenemedi"));
        }
    }

    // ========== QUERY ENDPOINTS ==========

    /**
     * Therapist'in tüm hastalarını getirme
     * Endpoint: GET /api/therapist-patient/{therapistId}/patients
     */
    @GetMapping("/{therapistId}/patients")
    public ResponseEntity<ApiResponse<List<PatientSummaryDto>>> getTherapistPatients(
            @PathVariable Long therapistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Therapist hastaları isteniyor: Therapist={}, Page={}, Size={}",
                    therapistId, page, size);

            List<PatientSummaryDto> patients = therapistPatientService.getTherapistPatients(therapistId, page, size);

            return ResponseEntity.ok(ApiResponse.success(patients, "Hasta listesi getirildi"));

        } catch (ResourceNotFoundException e) {
            log.error("Therapist hastaları getirme hatası: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Therapist hastaları getirme beklenmeyen hata: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Sistem hatası", "Hasta listesi getirilemedi"));
        }
    }

    /**
     * Belirli hasta özeti getirme
     * Endpoint: GET /api/therapist-patient/{therapistId}/patients/{patientId}
     */
    @GetMapping("/{therapistId}/patients/{patientId}")
    public ResponseEntity<ApiResponse<PatientSummaryDto>> getPatientSummary(
            @PathVariable Long therapistId,
            @PathVariable Long patientId) {
        try {
            log.info("Hasta özeti isteniyor: Therapist={}, Patient={}", therapistId, patientId);

            PatientSummaryDto patient = therapistPatientService.getPatientSummary(therapistId, patientId);

            return ResponseEntity.ok(ApiResponse.success(patient, "Hasta özeti getirildi"));

        } catch (ResourceNotFoundException e) {
            log.error("Hasta özeti getirme hatası: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Hasta özeti getirme beklenmeyen hata: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Sistem hatası", "Hasta özeti getirilemedi"));
        }
    }

    /**
     * Assignment geçmişi getirme
     * Endpoint: GET /api/therapist-patient/{therapistId}/history
     */
    @GetMapping("/{therapistId}/history")
    public ResponseEntity<ApiResponse<List<PatientSummaryDto>>> getAssignmentHistory(
            @PathVariable Long therapistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Assignment geçmişi isteniyor: Therapist={}, Page={}, Size={}",
                    therapistId, page, size);

            List<PatientSummaryDto> history = therapistPatientService.getAssignmentHistory(therapistId, page, size);

            return ResponseEntity.ok(ApiResponse.success(history, "Assignment geçmişi getirildi"));

        } catch (ResourceNotFoundException e) {
            log.error("Assignment geçmişi getirme hatası: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Assignment geçmişi getirme beklenmeyen hata: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Sistem hatası", "Assignment geçmişi getirilemedi"));
        }
    }

    // ========== SESSION MANAGEMENT ENDPOINTS ==========

    /**
     * Seans tamamlama
     * Endpoint: POST /api/therapist-patient/{therapistId}/appointments/{appointmentId}/complete
     */
    @PostMapping("/{therapistId}/appointments/{appointmentId}/complete")
    public ResponseEntity<ApiResponse<String>> completeSession(
            @PathVariable Long therapistId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody SessionCompletionRequest request) {
        try {
            log.info("Seans tamamlanıyor: Therapist={}, Appointment={}", therapistId, appointmentId);

            therapistPatientService.completeSession(therapistId, appointmentId, request);

            return ResponseEntity.ok(ApiResponse.success("OK", "Seans başarıyla tamamlandı"));

        } catch (ResourceNotFoundException e) {
            log.error("Seans tamamlama hatası: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Seans tamamlama beklenmeyen hata: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Sistem hatası", "Seans tamamlanamadı"));
        }
    }

    // ========== UTILITY ENDPOINTS ==========

    /**
     * Aktif assignment kontrolü
     * Endpoint: GET /api/therapist-patient/{therapistId}/patients/{patientId}/active
     */
    @GetMapping("/{therapistId}/patients/{patientId}/active")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveAssignment(
            @PathVariable Long therapistId,
            @PathVariable Long patientId) {
        try {
            log.info("Aktif assignment kontrolü: Therapist={}, Patient={}", therapistId, patientId);

            boolean hasActive = therapistPatientService.hasActiveAssignment(therapistId, patientId);

            return ResponseEntity.ok(ApiResponse.success(hasActive, "Assignment durumu kontrol edildi"));

        } catch (Exception e) {
            log.error("Assignment kontrol beklenmeyen hata: ", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Sistem hatası", "Assignment durumu kontrol edilemedi"));
        }
    }
}