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
    
    // Terapist bazlı sorgular
    List<TherapistPatient> findByTherapist(Therapist therapist);
    
    List<TherapistPatient> findByTherapistAndAssignmentStatus(Therapist therapist, AssignmentStatus status);
    
    Page<TherapistPatient> findByTherapist(Therapist therapist, Pageable pageable);
    
    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId")
    List<TherapistPatient> findByTherapistId(@Param("therapistId") Long therapistId);
    
    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.assignmentStatus = :status")
    List<TherapistPatient> findByTherapistIdAndAssignmentStatus(
        @Param("therapistId") Long therapistId, 
        @Param("status") AssignmentStatus status
    );
    
    // Hasta bazlı sorgular
    List<TherapistPatient> findByPatient(Patient patient);
    
    List<TherapistPatient> findByPatientAndAssignmentStatus(Patient patient, AssignmentStatus status);
    
    // Terapist-Hasta eşleşmesi
    Optional<TherapistPatient> findByTherapistAndPatient(Therapist therapist, Patient patient);
    
    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.patient.patientID = :patientId")
    Optional<TherapistPatient> findByTherapistIdAndPatientId(
        @Param("therapistId") Long therapistId, 
        @Param("patientId") Long patientId
    );
    
    // Aktif atamalar
    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.isActive = true")
    List<TherapistPatient> findAllActive();
    
    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.isActive = true")
    List<TherapistPatient> findActiveByTherapistId(@Param("therapistId") Long therapistId);
    
    // Öncelik bazlı sorgular
    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.priorityLevel >= :priority ORDER BY tp.priorityLevel DESC")
    List<TherapistPatient> findByTherapistIdAndPriorityLevelGreaterThanEqual(
        @Param("therapistId") Long therapistId, 
        @Param("priority") Integer priority
    );
    
    // Son aktivite bazlı sorgular
    @Query("SELECT tp FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.lastSessionDate < :date")
    List<TherapistPatient> findByTherapistIdAndLastSessionDateBefore(
        @Param("therapistId") Long therapistId, 
        @Param("date") LocalDateTime date
    );
    
    // İstatistik sorguları
    @Query("SELECT COUNT(tp) FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId AND tp.assignmentStatus = :status")
    Long countByTherapistIdAndAssignmentStatus(
        @Param("therapistId") Long therapistId, 
        @Param("status") AssignmentStatus status
    );
    
    @Query("SELECT AVG(tp.sessionsCompleted) FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId")
    Double getAverageSessionsCompletedByTherapistId(@Param("therapistId") Long therapistId);
    
    @Query("SELECT SUM(tp.outstandingBalance) FROM TherapistPatient tp WHERE tp.therapist.therapistId = :therapistId")
    Double getTotalOutstandingBalanceByTherapistId(@Param("therapistId") Long therapistId);
}

