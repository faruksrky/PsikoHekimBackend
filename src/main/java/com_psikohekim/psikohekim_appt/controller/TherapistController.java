package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.request.SessionCompletionRequest;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.dto.response.ApiResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistDashboardResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientSummaryDto;
import com_psikohekim.psikohekim_appt.dto.response.TherapistStatistics;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.service.TherapistService;
import com_psikohekim.psikohekim_appt.service.TherapistPatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/therapist")
@RequiredArgsConstructor
public class TherapistController {

    private final TherapistService therapistService;
    private final TherapistPatientService therapistPatientService;

    @PostMapping("/addTherapist")
    public TherapistResponse addTherapist(@RequestBody TherapistRequest therapist) throws ConflictException, InvalidRequestException {
        return therapistService.addTherapist(therapist);
    }

    @GetMapping("/all")
    public Map<String, List<TherapistResponse>> getTherapists() {
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

    /**
     * ID ile danışman getirme
     * GET /therapist/{therapistId}
     */
    @GetMapping("/{therapistId}")
    public ResponseEntity<TherapistResponse> getTherapistById(@PathVariable Long therapistId) {
        try {
            log.info("Danışman {} getiriliyor", therapistId);

            TherapistResponse therapist = therapistService.getTherapistById(therapistId);
            return ResponseEntity.ok(therapist);

        } catch (ResourceNotFoundException e) {
            log.error("Danışman bulunamadı: {}", therapistId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Danışman getirme hatası: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== YENİ DASHBOARD ENDPOINTS ==========

    /**
     * Terapist dashboard bilgilerini getirir
     */
    @GetMapping("/{therapistId}/dashboard")
    public ResponseEntity<ApiResponse<TherapistDashboardResponse>> getDashboard(
            @PathVariable Long therapistId) {
        try {
            log.info("Terapist {} dashboard bilgileri isteniyor", therapistId);

            TherapistDashboardResponse dashboard = therapistService.getTherapistDashboard(therapistId);

            return ResponseEntity.ok(ApiResponse.success(dashboard, "Dashboard bilgileri getirildi"));

        } catch (ResourceNotFoundException e) {
            log.error("Terapist bulunamadı: {}", therapistId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Dashboard getirme hatası: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "Dashboard bilgileri getirilemedi"));
        }
    }

    /**
     * Terapist hastalarını listeler
     */
    @GetMapping("/{therapistId}/patients")
    public ResponseEntity<ApiResponse<List<PatientSummaryDto>>> getTherapistPatients(
            @PathVariable Long therapistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Terapist {} hastaları isteniyor (sayfa: {}, boyut: {})", therapistId, page, size);

            List<PatientSummaryDto> patients = therapistPatientService.getTherapistPatients(therapistId, page, size);

            return ResponseEntity.ok(ApiResponse.success(patients, "Hasta listesi getirildi"));

        } catch (ResourceNotFoundException e) {
            log.error("Terapist bulunamadı: {}", therapistId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Hasta listesi getirme hatası: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "Hasta listesi getirilemedi"));
        }
    }

    /**
     * Terapist istatistiklerini getirir
     */
    @GetMapping("/{therapistId}/statistics")
    public ResponseEntity<ApiResponse<TherapistStatistics>> getStatistics(
            @PathVariable Long therapistId,
            @RequestParam(defaultValue = "WEEKLY") String period) {
        try {
            log.info("Terapist {} istatistikleri isteniyor (dönem: {})", therapistId, period);

            TherapistStatistics statistics = therapistService.getTherapistStatistics(therapistId, period);

            return ResponseEntity.ok(ApiResponse.success(statistics, "İstatistikler getirildi"));

        } catch (ResourceNotFoundException e) {
            log.error("Terapist bulunamadı: {}", therapistId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("İstatistik getirme hatası: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "İstatistikler getirilemedi"));
        }
    }

    /**
     * Seansı tamamlar
     */
    @PostMapping("/{therapistId}/sessions/{appointmentId}/complete")
    public ResponseEntity<ApiResponse<String>> completeSession(
            @PathVariable Long therapistId,
            @PathVariable Long appointmentId,
            @Valid @RequestBody SessionCompletionRequest request) {
        try {
            log.info("Terapist {} randevu {} tamamlanıyor", therapistId, appointmentId);

            therapistPatientService.completeSession(therapistId, appointmentId, request);

            return ResponseEntity.ok(ApiResponse.success("Seans tamamlandı", "Seans başarıyla tamamlandı"));

        } catch (ResourceNotFoundException e) {
            log.error("Randevu bulunamadı: {}", appointmentId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Seans tamamlama hatası: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "Seans tamamlanamadı"));
        }
    }

    /**
     * Hasta özetini getirir
     */
    @GetMapping("/{therapistId}/patients/{patientId}")
    public ResponseEntity<ApiResponse<PatientSummaryDto>> getPatientSummary(
            @PathVariable Long therapistId,
            @PathVariable Long patientId) {
        try {
            log.info("Terapist {} hasta {} özeti isteniyor", therapistId, patientId);

            PatientSummaryDto patient = therapistPatientService.getPatientSummary(therapistId, patientId);

            return ResponseEntity.ok(ApiResponse.success(patient, "Hasta özeti getirildi"));

        } catch (ResourceNotFoundException e) {
            log.error("Hasta ataması bulunamadı: therapistId={}, patientId={}", therapistId, patientId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Hasta özeti getirme hatası: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "Hasta özeti getirilemedi"));
        }
    }
}
