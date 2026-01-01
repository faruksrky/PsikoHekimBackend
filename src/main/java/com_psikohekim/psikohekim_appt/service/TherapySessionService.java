package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.request.SessionScheduleRequest;
import com_psikohekim.psikohekim_appt.dto.request.SessionUpdateRequest;
import com_psikohekim.psikohekim_appt.dto.response.SessionResponse;
import com_psikohekim.psikohekim_appt.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Therapy Session Service Interface
 * Normalized session management için
 */
public interface TherapySessionService {

    // ========== BASIC CRUD ==========

    /**
     * Yeni session oluştur
     */
    SessionResponse createSession(SessionScheduleRequest request);

    /**
     * Tüm session'ları getir
     */
    List<SessionResponse> getAllSessionsDesc();
    List<SessionResponse> getAllSessions();
    
    /**
     * Kullanıcı için session'ları getir (admin/user kontrolü ile)
     */
    List<SessionResponse> getAllSessionsForUser(String userEmail, boolean isAdmin, Long therapistId);
    /**
     * Session'ı güncelle
     */
    SessionResponse updateSession(Long sessionId, SessionUpdateRequest request);

    /**
     * Session'ı getir
     */
    SessionResponse getSession(Long sessionId);

    /**
     * Session'ı sil
     */
    void deleteSession(Long sessionId);

    /**
     * Hasta onayı - Session'ı onayla
     */
    SessionResponse approveSession(Long sessionId);

    /**
     * Hasta onayı - Session'ı reddet
     */
    SessionResponse rejectSession(Long sessionId);

    // ========== SESSION OPERATIONS ==========

    /**
     * Session'ı tamamla
     */
    SessionResponse completeSession(Long sessionId, String sessionNotes, String therapistNotes);

    /**
     * Session'ı iptal et
     */
    SessionResponse cancelSession(Long sessionId, String reason, String cancelledBy);

    /**
     * Session'ı ertele
     */
    SessionResponse rescheduleSession(Long sessionId, LocalDateTime newDate);

    /**
     * Session'ı başlat
     */
    SessionResponse startSession(Long sessionId);

    /**
     * No-show olarak işaretle
     */
    SessionResponse markAsNoShow(Long sessionId, String reason);

    // ========== LISTING ==========

    /**
     * Assignment'ın tüm session'ları
     */
    List<SessionResponse> getSessionsByAssignment(Long assignmentId);

    /**
     * Assignment'ın sayfalanmış session'ları
     */
    Page<SessionResponse> getSessionsByAssignment(Long assignmentId, Pageable pageable);

    /**
     * Therapist'in session'ları
     */
    List<SessionResponse> getSessionsByTherapist(Long therapistId);

    /**
     * Patient'ın session'ları
     */
    List<SessionResponse> getSessionsByPatient(Long patientId);

    /**
     * Status'a göre session'lar
     */
    List<SessionResponse> getSessionsByStatus(Long assignmentId, SessionStatus status);

    // ========== DATE BASED ==========

    /**
     * Therapist'in bugünkü session'ları
     */
    List<SessionResponse> getTodaySessionsByTherapist(Long therapistId);

    /**
     * Therapist'in bu haftaki session'ları
     */
    List<SessionResponse> getThisWeekSessionsByTherapist(Long therapistId);

    /**
     * Tarih aralığındaki session'lar
     */
    List<SessionResponse> getSessionsBetweenDates(Long assignmentId,
                                                  LocalDateTime startDate,
                                                  LocalDateTime endDate);

    /**
     * Geç kalmış session'lar
     */
    List<SessionResponse> getOverdueSessions();

    /**
     * Therapist'in geç kalmış session'ları
     */
    List<SessionResponse> getOverdueSessionsByTherapist(Long therapistId);

    // ========== ANALYTICS ==========

    /**
     * Assignment session istatistikleri
     */
    Map<String, Object> getAssignmentSessionStats(Long assignmentId);

    /**
     * Therapist'in aylık istatistikleri
     */
    Map<String, Object> getTherapistMonthlyStats(Long therapistId, int year, int month);

    /**
     * Therapist'in günlük özeti
     */
    Map<String, Object> getTherapistDailyStats(Long therapistId);

    /**
     * Completion rate hesapla
     */
    double getCompletionRate(Long assignmentId);

    /**
     * Attendance rate hesapla
     */
    double getAttendanceRate(Long assignmentId);

    /**
     * Assignment'ın toplam borcu
     */
    BigDecimal getTotalOutstandingBalance(Long assignmentId);

    // ========== PAYMENT ==========

    /**
     * Session ödemesini güncelle
     */
    SessionResponse updateSessionPayment(Long sessionId, String paymentStatus, String paymentMethod);

    /**
     * Ödenmemiş session'lar
     */
    List<SessionResponse> getUnpaidSessionsByTherapist(Long therapistId);

    // ========== SPECIFIC QUERIES ==========

    /**
     * Son tamamlanan session
     */
    SessionResponse getLastCompletedSession(Long assignmentId);

    /**
     * Bir sonraki scheduled session
     */
    SessionResponse getNextScheduledSession(Long assignmentId);

    /**
     * Session sayısı (status'a göre)
     */
    int countSessionsByStatus(Long assignmentId, SessionStatus status);

    // ========== BULK OPERATIONS ==========

    /**
     * Multiple session oluştur (recurring appointments için)
     */
    List<SessionResponse> createRecurringSessions(Long assignmentId,
                                                  LocalDateTime startDate,
                                                  int sessionCount,
                                                  int intervalDays,
                                                  BigDecimal sessionFee);

    /**
     * Assignment'ın scheduled session'larını iptal et
     */
    void cancelAllScheduledSessions(Long assignmentId, String reason);
}