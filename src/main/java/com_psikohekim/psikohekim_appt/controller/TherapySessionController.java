package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.SessionScheduleRequest;
import com_psikohekim.psikohekim_appt.dto.request.SessionUpdateRequest;
import com_psikohekim.psikohekim_appt.dto.response.SessionResponse;
import com_psikohekim.psikohekim_appt.enums.SessionStatus;
import com_psikohekim.psikohekim_appt.service.TherapySessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Therapy Session REST Controller
 * Comprehensive session management endpoints
 */
@RestController
@RequestMapping("/therapy-sessions")
@RequiredArgsConstructor
@Slf4j
public class TherapySessionController {

    private final TherapySessionService sessionService;

    @PostMapping("/addSession")
    public ResponseEntity<?> createSession(@RequestBody SessionScheduleRequest request) {
        try {
            log.info("Creating new session with request: {}", request);

            // Validation
            if (!request.isValid()) {
                log.error("Invalid session request: assignmentId={}, scheduledDate={}",
                        request.getAssignmentId(), request.getScheduledDate());
                return ResponseEntity.badRequest()
                        .body("Invalid session request: " +
                                (request.getAssignmentId() == null ? "assignmentId is required, " : "") +
                                (request.getScheduledDate() == null ? "scheduledDate is required, " : "") +
                                (request.getScheduledDate() != null && !request.getScheduledDate().isAfter(LocalDateTime.now().minusHours(1)) ?
                                        "scheduledDate must be at least 1 hour in the future" : ""));
            }

            SessionResponse response = sessionService.createSession(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating session: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating session: " + e.getMessage());
        }
    }

    @GetMapping("/getSessionsDesc")
    public ResponseEntity<List<SessionResponse>> getAllSessionsDesc() {
        try {
            List<SessionResponse> sessions = sessionService.getAllSessionsDesc();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error fetching all sessions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getSessions")
    public ResponseEntity<List<SessionResponse>> getAllSessions(
            @RequestParam(required = false) Long therapistId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // TODO: JWT token'dan user email ve isAdmin bilgisini al
            // Şimdilik therapistId parametresi ile çalışıyoruz
            // Admin olduğunu controller'da kontrol etmeliyiz
            boolean isAdmin = true; // TODO: JWT'den admin kontrolü yap
            String userEmail = ""; // TODO: JWT'den email al
            
            List<SessionResponse> sessions;
            if (therapistId != null) {
                // therapistId verilmişse o therapist'ın seanslarını getir
                sessions = sessionService.getSessionsByTherapist(therapistId);
            } else {
                // therapistId yoksa tüm seansları getir (admin kontrolü yapılmalı)
                sessions = sessionService.getAllSessionsForUser(userEmail, isAdmin, null);
            }
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error fetching all sessions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getSession/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable Long sessionId) {
        try {
            SessionResponse response = sessionService.getSession(sessionId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Session not found: {}", sessionId);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/updateSession/{sessionId}")
    public ResponseEntity<SessionResponse> updateSession(
            @PathVariable Long sessionId,
            @RequestBody SessionUpdateRequest request) {
        try {
            SessionResponse response = sessionService.updateSession(sessionId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/deleteSession/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        try {
            sessionService.deleteSession(sessionId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== SESSION OPERATIONS ==========

    /**
     * Session tamamla
     * POST /api/therapy-sessions/{sessionId}/complete
     */
    @PostMapping("/complete/{sessionId}")
    public ResponseEntity<SessionResponse> completeSession(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> notes) {
        try {
            String sessionNotes = notes.get("sessionNotes");
            String therapistNotes = notes.get("therapistNotes");

            SessionResponse response = sessionService.completeSession(sessionId, sessionNotes, therapistNotes);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error completing session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Session iptal et
     * POST /api/therapy-sessions/{sessionId}/cancel
     */
    @PostMapping("/cancel/{sessionId}")
    public ResponseEntity<SessionResponse> cancelSession(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> cancellationData) {
        try {
            String reason = cancellationData.get("reason");
            String cancelledBy = cancellationData.get("cancelledBy");

            SessionResponse response = sessionService.cancelSession(sessionId, reason, cancelledBy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error cancelling session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Session ertele
     * POST /api/therapy-sessions/{sessionId}/reschedule
     */
    @PostMapping("/reschedule/{sessionId}")
    public ResponseEntity<SessionResponse> rescheduleSession(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> rescheduleData) {
        try {
            LocalDateTime newDate = LocalDateTime.parse(rescheduleData.get("newDate"));

            SessionResponse response = sessionService.rescheduleSession(sessionId, newDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error rescheduling session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Session başlat
     * POST /api/therapy-sessions/{sessionId}/start
     */
    @PostMapping("/start/{sessionId}")
    public ResponseEntity<SessionResponse> startSession(@PathVariable Long sessionId) {
        try {
            SessionResponse response = sessionService.startSession(sessionId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error starting session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * No-show olarak işaretle
     * POST /api/therapy-sessions/{sessionId}/no-show
     */
    @PostMapping("/no-show/{sessionId}")
    public ResponseEntity<SessionResponse> markAsNoShow(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> data) {
        try {
            String reason = data.get("reason");
            SessionResponse response = sessionService.markAsNoShow(sessionId, reason);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error marking session {} as no-show: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== LISTING BY ASSIGNMENT ==========

    /**
     * Assignment'ın tüm session'ları
     * GET /api/therapy-sessions/assignment/{assignmentId}
     */
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByAssignment(@PathVariable Long assignmentId) {
        List<SessionResponse> sessions = sessionService.getSessionsByAssignment(assignmentId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Assignment'ın sayfalanmış session'ları
     * GET /api/therapy-sessions/assignment/{assignmentId}/paged
     */
    @GetMapping("/assignment/{assignmentId}/paged")
    public ResponseEntity<Page<SessionResponse>> getSessionsByAssignmentPaged(
            @PathVariable Long assignmentId,
            Pageable pageable) {
        Page<SessionResponse> sessions = sessionService.getSessionsByAssignment(assignmentId, pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Assignment'ın belirli status'taki session'ları
     * GET /api/therapy-sessions/assignment/{assignmentId}/status/{status}
     */
    @GetMapping("/assignment/{assignmentId}/status/{status}")
    public ResponseEntity<List<SessionResponse>> getSessionsByStatus(
            @PathVariable Long assignmentId,
            @PathVariable SessionStatus status) {
        List<SessionResponse> sessions = sessionService.getSessionsByStatus(assignmentId, status);
        return ResponseEntity.ok(sessions);
    }

    // ========== LISTING BY THERAPIST ==========

    /**
     * Therapist'in tüm session'ları
     * GET /api/therapy-sessions/therapist/{therapistId}
     */
    @GetMapping("/therapist/{therapistId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByTherapist(@PathVariable Long therapistId) {
        List<SessionResponse> sessions = sessionService.getSessionsByTherapist(therapistId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Therapist'in bugünkü session'ları
     * GET /api/therapy-sessions/therapist/{therapistId}/today
     */
    @GetMapping("/therapist/{therapistId}/today")
    public ResponseEntity<List<SessionResponse>> getTodaySessionsByTherapist(@PathVariable Long therapistId) {
        List<SessionResponse> sessions = sessionService.getTodaySessionsByTherapist(therapistId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Therapist'in bu haftaki session'ları
     * GET /api/therapy-sessions/therapist/{therapistId}/this-week
     */
    @GetMapping("/therapist/{therapistId}/this-week")
    public ResponseEntity<List<SessionResponse>> getThisWeekSessionsByTherapist(@PathVariable Long therapistId) {
        List<SessionResponse> sessions = sessionService.getThisWeekSessionsByTherapist(therapistId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Therapist'in geç kalmış session'ları
     * GET /api/therapy-sessions/therapist/{therapistId}/overdue
     */
    @GetMapping("/therapist/{therapistId}/overdue")
    public ResponseEntity<List<SessionResponse>> getOverdueSessionsByTherapist(@PathVariable Long therapistId) {
        List<SessionResponse> sessions = sessionService.getOverdueSessionsByTherapist(therapistId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Therapist'in ödenmemiş session'ları
     * GET /api/therapy-sessions/therapist/{therapistId}/unpaid
     */
    @GetMapping("/therapist/{therapistId}/unpaid")
    public ResponseEntity<List<SessionResponse>> getUnpaidSessionsByTherapist(@PathVariable Long therapistId) {
        List<SessionResponse> sessions = sessionService.getUnpaidSessionsByTherapist(therapistId);
        return ResponseEntity.ok(sessions);
    }

    // ========== LISTING BY PATIENT ==========

    /**
     * Patient'ın tüm session'ları
     * GET /api/therapy-sessions/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByPatient(@PathVariable Long patientId) {
        List<SessionResponse> sessions = sessionService.getSessionsByPatient(patientId);
        return ResponseEntity.ok(sessions);
    }

    // ========== DATE BASED QUERIES ==========

    /**
     * Tarih aralığındaki session'lar
     * GET /api/therapy-sessions/assignment/{assignmentId}/between
     */
    @GetMapping("/assignment/{assignmentId}/between")
    public ResponseEntity<List<SessionResponse>> getSessionsBetweenDates(
            @PathVariable Long assignmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SessionResponse> sessions = sessionService.getSessionsBetweenDates(assignmentId, startDate, endDate);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Tüm geç kalmış session'lar
     * GET /api/therapy-sessions/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<SessionResponse>> getOverdueSessions() {
        List<SessionResponse> sessions = sessionService.getOverdueSessions();
        return ResponseEntity.ok(sessions);
    }

    // ========== ANALYTICS ==========

    /**
     * Assignment session istatistikleri
     * GET /api/therapy-sessions/assignment/{assignmentId}/stats
     */
    @GetMapping("/assignment/{assignmentId}/stats")
    public ResponseEntity<Map<String, Object>> getAssignmentSessionStats(@PathVariable Long assignmentId) {
        Map<String, Object> stats = sessionService.getAssignmentSessionStats(assignmentId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Therapist'in aylık istatistikleri
     * GET /api/therapy-sessions/therapist/{therapistId}/stats/monthly
     */
    @GetMapping("/therapist/{therapistId}/stats/monthly")
    public ResponseEntity<Map<String, Object>> getTherapistMonthlyStats(
            @PathVariable Long therapistId,
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> stats = sessionService.getTherapistMonthlyStats(therapistId, year, month);
        return ResponseEntity.ok(stats);
    }

    /**
     * Therapist'in günlük özeti
     * GET /api/therapy-sessions/therapist/{therapistId}/stats/daily
     */
    @GetMapping("/therapist/{therapistId}/stats/daily")
    public ResponseEntity<Map<String, Object>> getTherapistDailyStats(@PathVariable Long therapistId) {
        Map<String, Object> stats = sessionService.getTherapistDailyStats(therapistId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Completion rate
     * GET /api/therapy-sessions/assignment/{assignmentId}/completion-rate
     */
    @GetMapping("/assignment/{assignmentId}/completion-rate")
    public ResponseEntity<Double> getCompletionRate(@PathVariable Long assignmentId) {
        double rate = sessionService.getCompletionRate(assignmentId);
        return ResponseEntity.ok(rate);
    }

    /**
     * Attendance rate
     * GET /api/therapy-sessions/assignment/{assignmentId}/attendance-rate
     */
    @GetMapping("/assignment/{assignmentId}/attendance-rate")
    public ResponseEntity<Double> getAttendanceRate(@PathVariable Long assignmentId) {
        double rate = sessionService.getAttendanceRate(assignmentId);
        return ResponseEntity.ok(rate);
    }

    /**
     * Toplam borç
     * GET /api/therapy-sessions/assignment/{assignmentId}/outstanding-balance
     */
    @GetMapping("/assignment/{assignmentId}/outstanding-balance")
    public ResponseEntity<BigDecimal> getTotalOutstandingBalance(@PathVariable Long assignmentId) {
        BigDecimal balance = sessionService.getTotalOutstandingBalance(assignmentId);
        return ResponseEntity.ok(balance);
    }

    // ========== PAYMENT ==========

    /**
     * Session ödemesini güncelle
     * PUT /api/therapy-sessions/{sessionId}/payment
     */
    @PutMapping("/{sessionId}/payment")
    public ResponseEntity<SessionResponse> updateSessionPayment(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> paymentData) {
        try {
            String paymentStatus = paymentData.get("paymentStatus");
            String paymentMethod = paymentData.get("paymentMethod");

            SessionResponse response = sessionService.updateSessionPayment(sessionId, paymentStatus, paymentMethod);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating payment for session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== SPECIFIC QUERIES ==========

    /**
     * Son tamamlanan session
     * GET /api/therapy-sessions/assignment/{assignmentId}/last-completed
     */
    @GetMapping("/assignment/{assignmentId}/last-completed")
    public ResponseEntity<SessionResponse> getLastCompletedSession(@PathVariable Long assignmentId) {
        SessionResponse session = sessionService.getLastCompletedSession(assignmentId);
        if (session != null) {
            return ResponseEntity.ok(session);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Bir sonraki scheduled session
     * GET /api/therapy-sessions/assignment/{assignmentId}/next-scheduled
     */
    @GetMapping("/assignment/{assignmentId}/next-scheduled")
    public ResponseEntity<SessionResponse> getNextScheduledSession(@PathVariable Long assignmentId) {
        SessionResponse session = sessionService.getNextScheduledSession(assignmentId);
        if (session != null) {
            return ResponseEntity.ok(session);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Session sayısı (status'a göre)
     * GET /api/therapy-sessions/assignment/{assignmentId}/count/{status}
     */
    @GetMapping("/assignment/{assignmentId}/count/{status}")
    public ResponseEntity<Integer> countSessionsByStatus(
            @PathVariable Long assignmentId,
            @PathVariable SessionStatus status) {
        int count = sessionService.countSessionsByStatus(assignmentId, status);
        return ResponseEntity.ok(count);
    }

    // ========== BULK OPERATIONS ==========

    /**
     * Recurring session'lar oluştur
     * POST /api/therapy-sessions/assignment/{assignmentId}/recurring
     */
    @PostMapping("/assignment/{assignmentId}/recurring")
    public ResponseEntity<List<SessionResponse>> createRecurringSessions(
            @PathVariable Long assignmentId,
            @RequestBody Map<String, Object> recurringData) {
        try {
            LocalDateTime startDate = LocalDateTime.parse((String) recurringData.get("startDate"));
            int sessionCount = (Integer) recurringData.get("sessionCount");
            int intervalDays = (Integer) recurringData.get("intervalDays");
            BigDecimal sessionFee = new BigDecimal(recurringData.get("sessionFee").toString());

            List<SessionResponse> sessions = sessionService.createRecurringSessions(
                    assignmentId, startDate, sessionCount, intervalDays, sessionFee);

            return ResponseEntity.status(HttpStatus.CREATED).body(sessions);
        } catch (Exception e) {
            log.error("Error creating recurring sessions for assignment {}: {}", assignmentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Assignment'ın tüm scheduled session'larını iptal et
     * POST /api/therapy-sessions/assignment/{assignmentId}/cancel-all
     */
    @PostMapping("/assignment/{assignmentId}/cancel-all")
    public ResponseEntity<Void> cancelAllScheduledSessions(
            @PathVariable Long assignmentId,
            @RequestBody Map<String, String> data) {
        try {
            String reason = data.get("reason");
            sessionService.cancelAllScheduledSessions(assignmentId, reason);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error cancelling all sessions for assignment {}: {}", assignmentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== PATIENT APPROVAL ==========

    /**
     * Hasta onayı - Seansı onayla
     * GET /api/therapy-sessions/approve/{sessionId}
     */
    @GetMapping("/approve/{sessionId}")
    public ResponseEntity<Map<String, Object>> approveSession(@PathVariable Long sessionId) {
        try {
            SessionResponse response = sessionService.approveSession(sessionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Seans başarıyla onaylandı!",
                    "session", response
            ));
        } catch (RuntimeException e) {
            log.error("Error approving session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Seans onaylanamadı: " + e.getMessage()
            ));
        }
    }

    /**
     * Hasta onayı - Seansı reddet
     * GET /api/therapy-sessions/reject/{sessionId}
     */
    @GetMapping("/reject/{sessionId}")
    public ResponseEntity<Map<String, Object>> rejectSession(@PathVariable Long sessionId) {
        try {
            SessionResponse response = sessionService.rejectSession(sessionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Seans reddedildi.",
                    "session", response
            ));
        } catch (RuntimeException e) {
            log.error("Error rejecting session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Seans reddedilemedi: " + e.getMessage()
            ));
        }
    }

    // ========== HEALTH CHECK ==========

    /**
     * Health check endpoint
     * GET /api/therapy-sessions/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "TherapySessionService",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}