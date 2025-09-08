package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.SessionScheduleRequest;
import com_psikohekim.psikohekim_appt.dto.request.SessionUpdateRequest;
import com_psikohekim.psikohekim_appt.dto.response.SessionResponse;
import com_psikohekim.psikohekim_appt.enums.SessionStatus;
import com_psikohekim.psikohekim_appt.mapper.TherapySessionMapper;
import com_psikohekim.psikohekim_appt.model.TherapySession;
import com_psikohekim.psikohekim_appt.model.TherapistPatient;
import com_psikohekim.psikohekim_appt.repository.TherapySessionRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistPatientRepository;
import com_psikohekim.psikohekim_appt.service.TherapySessionService;
import com_psikohekim.psikohekim_appt.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Comprehensive TherapySession Service Implementation
 * Normalized session management with full functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TherapySessionServiceImpl implements TherapySessionService {

    private final TherapySessionRepository sessionRepository;
    private final TherapistPatientRepository assignmentRepository;
    private final TherapySessionMapper sessionMapper;
    private final WhatsAppService whatsAppService;

    // ========== BASIC CRUD ==========

    @Override
    public SessionResponse createSession(SessionScheduleRequest request) {
        log.info("Creating new session for assignment: {}, patientId: {}", request.getAssignmentId(), request.getPatientId());

        // Assignment'ı bul - önce assignmentId, sonra patientId ile
        TherapistPatient assignment;
        if (request.getAssignmentId() != null) {
            assignment = assignmentRepository.findById(request.getAssignmentId())
                    .orElseThrow(() -> new RuntimeException("Assignment not found: " + request.getAssignmentId()));
        } else if (request.getPatientId() != null) {
            // patientId ile assignment bul (therapist bilgisi gerekli)
            // Bu durumda therapist bilgisini request'ten almalıyız veya context'ten
            throw new RuntimeException("patientId ile session oluşturma henüz desteklenmiyor. assignmentId kullanın.");
        } else {
            throw new RuntimeException("Either assignmentId or patientId must be provided");
        }

        // Session oluştur
        TherapySession session = new TherapySession();
        session.setAssignment(assignment);
        session.setTherapistId(assignment.getTherapist().getTherapistId());
        session.setPatientId(assignment.getPatient().getPatientId());
        session.setScheduledDate(request.getScheduledDate());
        session.setSessionFee(request.getSessionFee());
        session.setSessionType(request.getSessionType());
        session.setSessionFormat(request.getSessionFormat());
        session.setSessionNotes(request.getNotes());
        session.setStatus(SessionStatus.SCHEDULED);
        session.setPaymentStatus("PENDING");
        session.setCreatedBy("SYSTEM");
        session.setUpdatedBy("SYSTEM");

        TherapySession savedSession = sessionRepository.save(session);
        log.info("Session created successfully: {}", savedSession.getTherapySessionId());

        // WhatsApp mesajı gönder
        try {
            whatsAppService.sendAppointmentConfirmation(
                    assignment.getPatient(),
                    assignment.getTherapist(),
                    request.getScheduledDate()
            );
        } catch (Exception e) {
            log.error("WhatsApp mesajı gönderilemedi: {}", e.getMessage());
            // WhatsApp hatası session oluşturmayı engellemez
        }

        return sessionMapper.toResponseDto(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getAllSessionsDesc() {
        log.info("Fetching all therapy sessions based on schedule date desc");
        List<TherapySession> sessions = sessionRepository.findAllByOrderByScheduledDateDesc();
        return sessionMapper.toResponseDtoList(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getAllSessions() {
        log.info("Fetching all therapy sessions");
        List<TherapySession> sessions = sessionRepository.findAllWithPatientAndTherapistData();
        return sessionMapper.toResponseDtoList(sessions);
    }

    @Override
    public SessionResponse updateSession(Long sessionId, SessionUpdateRequest request) {
        log.info("Updating session: {}", sessionId);

        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // Status kontrolü - completed session'ları güncelleme kısıtlaması
        if (session.getStatus() == SessionStatus.COMPLETED &&
                request.getSessionNotes() == null && request.getTherapistNotes() == null) {
            throw new RuntimeException("Cannot modify completed session except notes");
        }

        // Update fields
        updateSessionFields(session, request);

        TherapySession updatedSession = sessionRepository.save(session);
        log.info("Session updated successfully: {}", sessionId);

        return sessionMapper.toResponseDto(updatedSession);
    }

    @Override
    public SessionResponse getSession(Long sessionId) {
        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        return sessionMapper.toResponseDto(session);
    }

    @Override
    public void deleteSession(Long sessionId) {
        log.info("Deleting session: {}", sessionId);

        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // Completed session'ları silme kısıtlaması
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Cannot delete completed session");
        }

        // Ödeme yapılmış session'ları silme kısıtlaması
        if (session.isPaid()) {
            throw new RuntimeException("Cannot delete paid session");
        }

        sessionRepository.delete(session);
        log.info("Session deleted successfully: {}", sessionId);
    }

    // ========== SESSION OPERATIONS ==========

    @Override
    public SessionResponse completeSession(Long sessionId, String sessionNotes, String therapistNotes) {
        log.info("Completing session: {}", sessionId);

        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Session already completed");
        }

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new RuntimeException("Cannot complete cancelled session");
        }

        // Session'ı tamamla
        session.markAsCompleted(sessionNotes, therapistNotes);
        session.setUpdatedBy("SYSTEM"); // TODO: Get from security context

        TherapySession completedSession = sessionRepository.save(session);
        log.info("Session completed successfully: {}", sessionId);

        return sessionMapper.toResponseDto(completedSession);
    }

    @Override
    public SessionResponse cancelSession(Long sessionId, String reason, String cancelledBy) {
        log.info("Cancelling session: {} by {}", sessionId, cancelledBy);

        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed session");
        }

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new RuntimeException("Session already cancelled");
        }

        // Session'ı iptal et
        session.markAsCancelled(reason, cancelledBy);
        session.setUpdatedBy(cancelledBy);

        TherapySession cancelledSession = sessionRepository.save(session);
        log.info("Session cancelled successfully: {}", sessionId);

        return sessionMapper.toResponseDto(cancelledSession);
    }

    @Override
    public SessionResponse rescheduleSession(Long sessionId, LocalDateTime newDate) {
        log.info("Rescheduling session: {} to {}", sessionId, newDate);

        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Cannot reschedule completed session");
        }

        if (newDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot reschedule to past date");
        }

        // Time conflict kontrolü
        boolean conflictExists = sessionRepository.findByTherapistIdAndStatus(
                        session.getTherapistId(), SessionStatus.SCHEDULED)
                .stream()
                .filter(s -> !s.getTherapySessionId().equals(sessionId))
                .anyMatch(s -> isTimeConflict(s.getScheduledDate(), newDate));

        if (conflictExists) {
            throw new RuntimeException("Time conflict with existing session");
        }

        // Reschedule
        session.setScheduledDate(newDate);
        session.setStatus(SessionStatus.SCHEDULED);
        session.setUpdatedBy("SYSTEM");

        TherapySession rescheduledSession = sessionRepository.save(session);
        log.info("Session rescheduled successfully: {}", sessionId);

        return sessionMapper.toResponseDto(rescheduledSession);
    }

    @Override
    public SessionResponse startSession(Long sessionId) {
        log.info("Starting session: {}", sessionId);

        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled sessions can be started");
        }

        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setActualStartTime(LocalDateTime.now());
        session.setUpdatedBy("SYSTEM");

        TherapySession startedSession = sessionRepository.save(session);
        log.info("Session started successfully: {}", sessionId);

        return sessionMapper.toResponseDto(startedSession);
    }

    @Override
    public SessionResponse markAsNoShow(Long sessionId, String reason) {
        log.info("Marking session as no-show: {}", sessionId);

        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled sessions can be marked as no-show");
        }

        session.setStatus(SessionStatus.NO_SHOW);
        session.setCancellationReason(reason);
        session.setCancelledBy("SYSTEM");
        session.setCancelledAt(LocalDateTime.now());
        session.setUpdatedBy("SYSTEM");

        TherapySession noShowSession = sessionRepository.save(session);
        log.info("Session marked as no-show: {}", sessionId);

        return sessionMapper.toResponseDto(noShowSession);
    }

    // ========== LISTING ==========

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByAssignment(Long assignmentId) {
        List<TherapySession> sessions = sessionRepository.findByAssignment_TherapistPatientIdOrderByScheduledDateDesc(assignmentId);
        return sessionMapper.toResponseDtoList(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getSessionsByAssignment(Long assignmentId, Pageable pageable) {
        Page<TherapySession> sessions = sessionRepository.findByAssignment_TherapistPatientIdOrderByScheduledDateDesc(assignmentId, pageable);
        return sessions.map(sessionMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByTherapist(Long therapistId) {
        List<TherapySession> sessions = sessionRepository.findByTherapistIdOrderByScheduledDateDesc(therapistId);
        return sessionMapper.toResponseDtoList(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByPatient(Long patientId) {
        List<TherapySession> sessions = sessionRepository.findByPatientIdOrderByScheduledDateDesc(patientId);
        return sessionMapper.toResponseDtoList(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByStatus(Long assignmentId, SessionStatus status) {
        List<TherapySession> sessions = sessionRepository.findByAssignment_TherapistPatientIdAndStatus(assignmentId, status);
        return sessionMapper.toResponseDtoList(sessions);
    }

    // ========== DATE BASED ==========

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getTodaySessionsByTherapist(Long therapistId) {
        // Bugünün başlangıç ve bitiş saatlerini hesapla
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<TherapySession> sessions = sessionRepository.findTodaySessionsByTherapist(therapistId, startOfDay, endOfDay);
        return sessionMapper.toResponseDtoList(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getThisWeekSessionsByTherapist(Long therapistId) {
        LocalDateTime weekStart = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekEnd = weekStart.plusWeeks(1);

        List<TherapySession> sessions = sessionRepository.findThisWeekSessionsByTherapist(therapistId, weekStart, weekEnd);
        return sessionMapper.toResponseDtoList(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsBetweenDates(Long assignmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<TherapySession> sessions = sessionRepository.findSessionsBetweenDates(assignmentId, startDate, endDate);
        return sessionMapper.toResponseDtoList(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getOverdueSessions() {
        List<TherapySession> sessions = sessionRepository.findOverdueSessions();
        return sessionMapper.toResponseDtoList(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getOverdueSessionsByTherapist(Long therapistId) {
        List<TherapySession> sessions = sessionRepository.findOverdueSessionsByTherapist(therapistId);
        return sessionMapper.toResponseDtoList(sessions);
    }

    // ========== ANALYTICS ==========

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAssignmentSessionStats(Long assignmentId) {
        Map<String, Object> stats = new HashMap<>();

        int completed = sessionRepository.countCompletedSessionsByAssignment(assignmentId);
        int cancelled = sessionRepository.countCancelledSessionsByAssignment(assignmentId);

        List<TherapySession> allSessions = sessionRepository.findByAssignment_TherapistPatientIdOrderByScheduledDateDesc(assignmentId);
        int total = allSessions.size();
        int scheduled = (int) allSessions.stream().filter(s -> s.getStatus() == SessionStatus.SCHEDULED).count();
        int noShow = (int) allSessions.stream().filter(s -> s.getStatus() == SessionStatus.NO_SHOW).count();

        stats.put("totalSessions", total);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        stats.put("scheduled", scheduled);
        stats.put("noShow", noShow);
        stats.put("completionRate", total > 0 ? (double) completed / total * 100 : 0.0);
        stats.put("attendanceRate", getAttendanceRate(assignmentId));

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTherapistMonthlyStats(Long therapistId, int year, int month) {
        Map<String, Object> stats = new HashMap<>();

        List<Object[]> monthlyStats = sessionRepository.getMonthlyStatsByTherapist(therapistId, year, month);

        int completed = 0, cancelled = 0, scheduled = 0, noShow = 0;

        for (Object[] stat : monthlyStats) {
            String status = (String) stat[0];
            Long count = (Long) stat[1];

            switch (status) {
                case "COMPLETED" -> completed = count.intValue();
                case "CANCELLED" -> cancelled = count.intValue();
                case "SCHEDULED" -> scheduled = count.intValue();
                case "NO_SHOW" -> noShow = count.intValue();
            }
        }

        int total = completed + cancelled + scheduled + noShow;

        stats.put("year", year);
        stats.put("month", month);
        stats.put("totalSessions", total);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        stats.put("scheduled", scheduled);
        stats.put("noShow", noShow);
        stats.put("completionRate", total > 0 ? (double) completed / total * 100 : 0.0);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTherapistDailyStats(Long therapistId) {
        Map<String, Object> stats = new HashMap<>();

        // Bugünün başlangıç ve bitiş saatlerini hesapla
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Object[] dailyStats = sessionRepository.getDailyStatsByTherapist(therapistId, startOfDay, endOfDay);

        if (dailyStats != null && dailyStats.length >= 3) {
            Long completed = (Long) dailyStats[0];
            Long cancelled = (Long) dailyStats[1];
            Long scheduled = (Long) dailyStats[2];

            stats.put("completed", completed != null ? completed.intValue() : 0);
            stats.put("cancelled", cancelled != null ? cancelled.intValue() : 0);
            stats.put("scheduled", scheduled != null ? scheduled.intValue() : 0);
            stats.put("total", (completed != null ? completed : 0L) +
                    (cancelled != null ? cancelled : 0L) +
                    (scheduled != null ? scheduled : 0L));
        } else {
            stats.put("completed", 0);
            stats.put("cancelled", 0);
            stats.put("scheduled", 0);
            stats.put("total", 0);
        }

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public double getCompletionRate(Long assignmentId) {
        TherapistPatient assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        return assignment.getCompletionRate();
    }

    @Override
    @Transactional(readOnly = true)
    public double getAttendanceRate(Long assignmentId) {
        TherapistPatient assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        return assignment.getAttendanceRate();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalOutstandingBalance(Long assignmentId) {
        return sessionRepository.getTotalOutstandingBalance(assignmentId);
    }

    // ========== PAYMENT ==========

    @Override
    public SessionResponse updateSessionPayment(Long sessionId, String paymentStatus, String paymentMethod) {
        log.info("Updating payment for session: {} to status: {}", sessionId, paymentStatus);

        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new RuntimeException("Only completed sessions can have payment updates");
        }

        session.setPaymentStatus(paymentStatus);
        session.setPaymentMethod(paymentMethod);
        session.setUpdatedBy("SYSTEM");

        TherapySession updatedSession = sessionRepository.save(session);
        log.info("Payment updated for session: {}", sessionId);

        return sessionMapper.toResponseDto(updatedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getUnpaidSessionsByTherapist(Long therapistId) {
        List<TherapySession> sessions = sessionRepository.findUnpaidSessionsByTherapist(therapistId);
        return sessionMapper.toResponseDtoList(sessions);
    }

    // ========== SPECIFIC QUERIES ==========

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getLastCompletedSession(Long assignmentId) {
        Optional<TherapySession> session = sessionRepository.findFirstByAssignment_TherapistPatientIdAndStatusOrderByScheduledDateDesc(
                assignmentId, SessionStatus.COMPLETED);

        return session.map(sessionMapper::toResponseDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getNextScheduledSession(Long assignmentId) {
        Optional<TherapySession> session = sessionRepository.findNextScheduledSession(assignmentId);
        return session.map(sessionMapper::toResponseDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public int countSessionsByStatus(Long assignmentId, SessionStatus status) {
        List<TherapySession> sessions = sessionRepository.findByAssignment_TherapistPatientIdAndStatus(assignmentId, status);
        return sessions.size();
    }

    // ========== BULK OPERATIONS ==========

    @Override
    public List<SessionResponse> createRecurringSessions(Long assignmentId, LocalDateTime startDate,
                                                         int sessionCount, int intervalDays, BigDecimal sessionFee) {
        log.info("Creating {} recurring sessions for assignment: {}", sessionCount, assignmentId);

        TherapistPatient assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        if (!assignment.isActiveAssignment()) {
            throw new RuntimeException("Cannot create sessions for inactive assignment");
        }

        List<TherapySession> sessions = new ArrayList<>();
        LocalDateTime currentDate = startDate;

        for (int i = 0; i < sessionCount; i++) {
            // Time conflict kontrolü
            final LocalDateTime sessionDate = currentDate;
            boolean conflictExists = sessionRepository.findByTherapistIdAndStatus(
                            assignment.getTherapist().getTherapistId(), SessionStatus.SCHEDULED)
                    .stream()
                    .anyMatch(session -> isTimeConflict(session.getScheduledDate(), sessionDate));

            if (conflictExists) {
                log.warn("Skipping session creation due to time conflict at: {}", currentDate);
                currentDate = currentDate.plusDays(intervalDays);
                continue;
            }

            TherapySession session = TherapySession.builder()
                    .assignment(assignment)
                    .scheduledDate(currentDate)
                    .sessionFee(sessionFee)
                    .sessionType("REGULAR")
                    .sessionFormat("IN_PERSON")
                    .status(SessionStatus.SCHEDULED)
                    .createdBy("SYSTEM")
                    .build();

            sessions.add(session);
            currentDate = currentDate.plusDays(intervalDays);
        }

        List<TherapySession> savedSessions = sessionRepository.saveAll(sessions);
        log.info("Created {} recurring sessions successfully", savedSessions.size());

        return sessionMapper.toResponseDtoList(savedSessions);
    }

    @Override
    public void cancelAllScheduledSessions(Long assignmentId, String reason) {
        log.info("Cancelling all scheduled sessions for assignment: {}", assignmentId);

        List<TherapySession> scheduledSessions = sessionRepository.findByAssignment_TherapistPatientIdAndStatus(
                assignmentId, SessionStatus.SCHEDULED);

        for (TherapySession session : scheduledSessions) {
            session.markAsCancelled(reason, "SYSTEM");
            session.setUpdatedBy("SYSTEM");
        }

        sessionRepository.saveAll(scheduledSessions);
        log.info("Cancelled {} scheduled sessions for assignment: {}", scheduledSessions.size(), assignmentId);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private boolean isTimeConflict(LocalDateTime existingTime, LocalDateTime newTime) {
        // 1 saatlik session süresini varsayıyoruz
        LocalDateTime existingEnd = existingTime.plusHours(1);
        LocalDateTime newEnd = newTime.plusHours(1);

        return !(newTime.isAfter(existingEnd) || newEnd.isBefore(existingTime));
    }

    private void updateSessionFields(TherapySession session, SessionUpdateRequest request) {
        // Session details
        if (request.getSessionFee() != null) {
            session.setSessionFee(request.getSessionFee());
        }
        if (request.getSessionType() != null) {
            session.setSessionType(request.getSessionType());
        }
        if (request.getSessionFormat() != null) {
            session.setSessionFormat(request.getSessionFormat());
        }

        if (request.getSessionNotes() != null) {
            session.setSessionNotes(request.getSessionNotes());
        }
        if (request.getTherapistNotes() != null) {
            session.setTherapistNotes(request.getTherapistNotes());
        }
        if (request.getPatientFeedback() != null) {
            session.setPatientFeedback(request.getPatientFeedback());
        }
        if (request.getHomeworkAssigned() != null) {
            session.setHomeworkAssigned(request.getHomeworkAssigned());
        }
        if (request.getNextSessionGoals() != null) {
            session.setNextSessionGoals(request.getNextSessionGoals());
        }
        if (request.getSessionRating() != null) {
            session.setSessionRating(request.getSessionRating());
        }
        if (request.getActualStartTime() != null) {
            session.setActualStartTime(request.getActualStartTime());
        }
        if (request.getActualEndTime() != null) {
            session.setActualEndTime(request.getActualEndTime());
        }
        if (request.getPaymentStatus() != null) {
            session.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getPaymentMethod() != null) {
            session.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getCancellationReason() != null) {
            session.setCancellationReason(request.getCancellationReason());
        }
        if (request.getCancelledBy() != null) {
            session.setCancelledBy(request.getCancelledBy());
        }
        if (request.getNewScheduledDate() != null) {
            session.setScheduledDate(request.getNewScheduledDate());
        }

        session.setUpdatedBy("SYSTEM"); // TODO: Get from security context
    }
}