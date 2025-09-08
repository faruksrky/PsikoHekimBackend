package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.Payment;
import com_psikohekim.psikohekim_appt.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPatient(Patient patient);

    List<Payment> findByPatientOrderByPaymentDateDesc(Patient patient);

    @Query("SELECT p FROM Payment p WHERE p.patient.patientId = :patientId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByPatientIdAndPaymentDateBetween(
            @Param("patientId") Long patientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status")
    List<Payment> findByPaymentStatus(@Param("status") String status);

    @Query("SELECT p FROM Payment p WHERE p.patient.patientId = :patientId AND p.paymentStatus = :status")
    List<Payment> findByPatientIdAndPaymentStatus(
            @Param("patientId") Long patientId,
            @Param("status") String status
    );

    @Query("SELECT SUM(p.amountPatient) FROM Payment p WHERE p.patient.patientId = :patientId")
    Double getTotalAmountByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT SUM(p.amountPatient) FROM Payment p WHERE p.patient.patientId= :patientId AND p.paymentStatus = :status")
    Double getTotalAmountByPatientIdAndStatus(
            @Param("patientId") Long patientId,
            @Param("status") String status
    );
}