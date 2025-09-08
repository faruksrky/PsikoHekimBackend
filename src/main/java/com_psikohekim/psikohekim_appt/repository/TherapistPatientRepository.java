package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.TherapistPatient;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.enums.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TherapistPatientRepository extends JpaRepository<TherapistPatient, Long> {

    // ========== OTOMATİK METHOD NAMING (Spring Data JPA) ==========

    // Terapist bazlı - otomatik
    List<TherapistPatient> findByTherapist(Therapist therapist);
    List<TherapistPatient> findByTherapistAndAssignmentStatus(Therapist therapist, AssignmentStatus status);
    Page<TherapistPatient> findByTherapist(Therapist therapist, Pageable pageable);

    // Hasta bazlı - otomatik
    List<TherapistPatient> findByPatient(Patient patient);
    List<TherapistPatient> findByPatientAndAssignmentStatus(Patient patient, AssignmentStatus status);

    // Terapist-Hasta eşleşmesi - otomatik
    Optional<TherapistPatient> findByTherapistAndPatient(Therapist therapist, Patient patient);

    // Aktif atamalar - otomatik
    List<TherapistPatient> findByIsActiveTrue();
    List<TherapistPatient> findByIsActiveFalse();

    // Öncelik bazlı - otomatik
    List<TherapistPatient> findByPriorityLevelGreaterThanEqualOrderByPriorityLevelDesc(Integer priority);

    // ========== CUSTOM JPQL QUERIES (Performans + Okunabilirlik) ==========

    // Nested property access için (therapist.therapistId)
    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId")
    List<TherapistPatient> findByTherapistId(@Param("therapistId") Long therapistId);

    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.assignmentStatus = :status")
    List<TherapistPatient> findByTherapistIdAndAssignmentStatus(
            @Param("therapistId") Long therapistId,
            @Param("status") AssignmentStatus status
    );

    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.patient.patientId = :patientId")
    Optional<TherapistPatient> findByTherapistIdAndPatientId(
            @Param("therapistId") Long therapistId,
            @Param("patientId") Long patientId
    );

    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.isActive = true")
    List<TherapistPatient> findActiveByTherapistId(@Param("therapistId") Long therapistId);

    // Complex filtering (method naming çok uzun olurdu)
    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.priorityLevel >= :priority ORDER BY tp.priorityLevel DESC, tp.updatedAt DESC")
    List<TherapistPatient> findHighPriorityByTherapistId(
            @Param("therapistId") Long therapistId,
            @Param("priority") Integer priority
    );

    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.updatedAt < :date ORDER BY tp.updatedAt ASC")
    List<TherapistPatient> findInactivePatientsByTherapistId(
            @Param("therapistId") Long therapistId,
            @Param("date") LocalDateTime date
    );

    // ========== AGGREGATE FUNCTIONS (Sadece custom query ile mümkün) ==========

    @Query("SELECT COUNT(tp) FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.assignmentStatus = :status")
    Long countByTherapistIdAndAssignmentStatus(
            @Param("therapistId") Long therapistId,
            @Param("status") AssignmentStatus status
    );

    // ========== NORMALIZED SESSION QUERIES (JOIN with TherapySession) ==========

    @Query("SELECT AVG(SIZE(tp.sessions)) FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId")
    Double getAverageSessionsCompletedByTherapistId(@Param("therapistId") Long therapistId);

    @Query("SELECT COALESCE(SUM(ts.sessionFee), 0.0) FROM TherapistPatient tp " +
            "JOIN tp.sessions ts WHERE tp.therapist.therapistId = :therapistId " +
            "AND ts.status = 'COMPLETED' AND ts.paymentStatus != 'PAID'")
    Double getTotalOutstandingBalanceByTherapistId(@Param("therapistId") Long therapistId);

    @Query("SELECT COALESCE(SUM(ts.sessionFee), 0.0) FROM TherapistPatient tp " +
            "JOIN tp.sessions ts WHERE tp.therapist.therapistId = :therapistId " +
            "AND ts.paymentStatus = 'PAID'")
    Double getTotalAmountPaidByTherapistId(@Param("therapistId") Long therapistId);

    // Kompleks istatistikler
    @Query("SELECT tp.assignmentStatus, COUNT(tp) FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId GROUP BY tp.assignmentStatus")
    List<Object[]> getStatusStatisticsByTherapistId(@Param("therapistId") Long therapistId);
}

