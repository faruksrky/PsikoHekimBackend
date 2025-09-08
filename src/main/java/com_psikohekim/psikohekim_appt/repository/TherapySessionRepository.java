package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.enums.SessionStatus;
import com_psikohekim.psikohekim_appt.model.TherapySession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TherapySession repository
 * Normalized session data için query metodları
 */
public interface TherapySessionRepository extends JpaRepository<TherapySession, Long> {

    // ========== BASIC QUERIES ==========

    /**
     * Tüm session'ları tarihe göre sıralayarak getir
     */
    List<TherapySession> findAllByOrderByScheduledDateDesc();

    /**
     * Tüm session'ları patient ve therapist bilgileriyle birlikte getir (N+1 problemi önlemek için)
     */
    @Query("SELECT DISTINCT ts FROM TherapySession ts " +
            "LEFT JOIN FETCH ts.patient p " +
            "LEFT JOIN FETCH ts.therapist t " +
            "ORDER BY ts.scheduledDate DESC")
    List<TherapySession> findAllWithPatientAndTherapistData();

    /**
     * Assignment'a göre tüm session'ları getir
     */
    List<TherapySession> findByAssignment_TherapistPatientIdOrderByScheduledDateDesc(Long assignmentId);

    /**
     * Assignment'a göre sayfalanmış session'ları getir
     */
    Page<TherapySession> findByAssignment_TherapistPatientIdOrderByScheduledDateDesc(Long assignmentId, Pageable pageable);

    /**
     * Therapist'in tüm session'ları
     */
    List<TherapySession> findByTherapistIdOrderByScheduledDateDesc(Long therapistId);

    /**
     * Patient'ın tüm session'ları
     */
    List<TherapySession> findByPatientIdOrderByScheduledDateDesc(Long patientId);

    // ========== STATUS BASED QUERIES ==========

    /**
     * Assignment'a göre belirli status'taki session'lar
     */
    List<TherapySession> findByAssignment_TherapistPatientIdAndStatus(Long assignmentId, SessionStatus status);

    /**
     * Therapist'in belirli status'taki session'ları
     */
    List<TherapySession> findByTherapistIdAndStatus(Long therapistId, SessionStatus status);

    /**
     * Patient'ın belirli status'taki session'ları
     */
    List<TherapySession> findByPatientIdAndStatus(Long patientId, SessionStatus status);

    // ========== DATE BASED QUERIES ==========

    /**
     * Therapist'in bugünkü session'ları
     */
    /**
     * Therapist'in bugünkü session'ları
     */
    @Query("SELECT ts FROM TherapySession ts WHERE ts.therapistId = :therapistId " +
            "AND ts.scheduledDate >= :startOfDay AND ts.scheduledDate < :endOfDay " +
            "ORDER BY ts.scheduledDate")
    List<TherapySession> findTodaySessionsByTherapist(@Param("therapistId") Long therapistId,
                                                      @Param("startOfDay") LocalDateTime startOfDay,
                                                      @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Therapist'in bu haftaki session'ları
     */
    @Query("SELECT ts FROM TherapySession ts WHERE ts.therapistId = :therapistId " +
            "AND ts.scheduledDate >= :weekStart AND ts.scheduledDate < :weekEnd " +
            "ORDER BY ts.scheduledDate")
    List<TherapySession> findThisWeekSessionsByTherapist(@Param("therapistId") Long therapistId,
                                                         @Param("weekStart") LocalDateTime weekStart,
                                                         @Param("weekEnd") LocalDateTime weekEnd);

    /**
     * Tarih aralığındaki session'lar
     */
    @Query("SELECT ts FROM TherapySession ts WHERE ts.assignment.therapistPatientId = :assignmentId " +
            "AND ts.scheduledDate >= :startDate AND ts.scheduledDate <= :endDate " +
            "ORDER BY ts.scheduledDate")
    List<TherapySession> findSessionsBetweenDates(@Param("assignmentId") Long assignmentId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // ========== ANALYTICS QUERIES ==========

    /**
     * Assignment'ın tamamlanan session sayısı
     */
    @Query("SELECT COUNT(ts) FROM TherapySession ts WHERE ts.assignment.therapistPatientId = :assignmentId AND ts.status = 'COMPLETED'")
    int countCompletedSessionsByAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * Assignment'ın iptal edilen session sayısı
     */
    @Query("SELECT COUNT(ts) FROM TherapySession ts WHERE ts.assignment.therapistPatientId = :assignmentId " +
            "AND ts.status IN ('CANCELLED', 'NO_SHOW')")
    int countCancelledSessionsByAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * Therapist'in aylık istatistikleri
     */
    @Query("SELECT ts.status, COUNT(ts) FROM TherapySession ts WHERE ts.therapistId = :therapistId " +
            "AND YEAR(ts.scheduledDate) = :year AND MONTH(ts.scheduledDate) = :month " +
            "GROUP BY ts.status")
    List<Object[]> getMonthlyStatsByTherapist(@Param("therapistId") Long therapistId,
                                              @Param("year") int year,
                                              @Param("month") int month);

    // ========== SPECIFIC QUERIES ==========

    /**
     * Geç kalmış (overdue) session'lar
     */
    @Query("SELECT ts FROM TherapySession ts WHERE ts.status = 'SCHEDULED' " +
            "AND ts.scheduledDate < CURRENT_TIMESTAMP " +
            "ORDER BY ts.scheduledDate")
    List<TherapySession> findOverdueSessions();

    /**
     * Therapist'in geç kalmış session'ları
     */
    @Query("SELECT ts FROM TherapySession ts WHERE ts.therapistId = :therapistId " +
            "AND ts.status = 'SCHEDULED' AND ts.scheduledDate < CURRENT_TIMESTAMP " +
            "ORDER BY ts.scheduledDate")
    List<TherapySession> findOverdueSessionsByTherapist(@Param("therapistId") Long therapistId);

    /**
     * Son session'ı bul
     */
    Optional<TherapySession> findFirstByAssignment_TherapistPatientIdAndStatusOrderByScheduledDateDesc(
            Long assignmentId, SessionStatus status);

    /**
     * Bir sonraki scheduled session
     */
    @Query("SELECT ts FROM TherapySession ts WHERE ts.assignment.therapistPatientId = :assignmentId " +
            "AND ts.status = 'SCHEDULED' AND ts.scheduledDate > CURRENT_TIMESTAMP " +
            "ORDER BY ts.scheduledDate ASC")
    Optional<TherapySession> findNextScheduledSession(@Param("assignmentId") Long assignmentId);

    // ========== PAYMENT QUERIES ==========

    /**
     * Ödenmemiş session'lar
     */
    @Query("SELECT ts FROM TherapySession ts WHERE ts.therapistId = :therapistId " +
            "AND ts.status = 'COMPLETED' AND ts.paymentStatus IN ('PENDING', 'PARTIAL') " +
            "ORDER BY ts.scheduledDate")
    List<TherapySession> findUnpaidSessionsByTherapist(@Param("therapistId") Long therapistId);

    /**
     * Assignment'ın toplam borcu
     */
    @Query("SELECT COALESCE(SUM(ts.sessionFee), 0) FROM TherapySession ts " +
            "WHERE ts.assignment.therapistPatientId = :assignmentId AND ts.paymentStatus IN ('PENDING', 'PARTIAL')")
    java.math.BigDecimal getTotalOutstandingBalance(@Param("assignmentId") Long assignmentId);

    // ========== DASHBOARD QUERIES ==========

    /**
     * Therapist dashboard için günlük özet
     */
    /**
     * Therapist dashboard için günlük özet
     */
    @Query("SELECT " +
            "COUNT(CASE WHEN ts.status = 'COMPLETED' THEN 1 END) as completed, " +
            "COUNT(CASE WHEN ts.status = 'CANCELLED' THEN 1 END) as cancelled, " +
            "COUNT(CASE WHEN ts.status = 'SCHEDULED' THEN 1 END) as scheduled " +
            "FROM TherapySession ts WHERE ts.therapistId = :therapistId " +
            "AND ts.scheduledDate >= :startOfDay AND ts.scheduledDate < :endOfDay")
    Object[] getDailyStatsByTherapist(@Param("therapistId") Long therapistId,
                                      @Param("startOfDay") LocalDateTime startOfDay,
                                      @Param("endOfDay") LocalDateTime endOfDay);
}