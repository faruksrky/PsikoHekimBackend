package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.Appointment;
import com_psikohekim.psikohekim_appt.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.therapistId = :therapistId AND a.startTime BETWEEN :startTime AND :endTime")
    List<Appointment> findByTherapistIdAndStartTimeBetween(
            @Param("therapistId") Long therapistId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT a FROM Appointment a WHERE a.therapistId = :therapistId AND a.patientId = :patientId ORDER BY a.startTime DESC")
    List<Appointment> findByTherapistIdAndPatientIdOrderByStartTimeDesc(
            @Param("therapistId") Long therapistId,
            @Param("patientId") Long patientId
    );

    @Query("SELECT a FROM Appointment a WHERE a.therapistId = :therapistId")
    List<Appointment> findByTherapistId(@Param("therapistId") Long therapistId);

    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId")
    List<Appointment> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT a FROM Appointment a WHERE a.therapistId = :therapistId AND a.appointmentStatus = :status")
    List<Appointment> findByTherapistIdAndAppointmentStatus(
            @Param("therapistId") Long therapistId,
            @Param("status") AppointmentStatus status
    );

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.therapistId = :therapistId AND a.appointmentStatus = :status")
    Long countByTherapistIdAndAppointmentStatus(
            @Param("therapistId") Long therapistId,
            @Param("status") AppointmentStatus status
    );
}