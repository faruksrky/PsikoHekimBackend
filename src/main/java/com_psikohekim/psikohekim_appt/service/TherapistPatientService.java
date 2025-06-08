package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.request.BpmnAssignmentRequest;
import com_psikohekim.psikohekim_appt.dto.request.SessionCompletionRequest;
import com_psikohekim.psikohekim_appt.dto.response.AssignResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientSummaryDto;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;

import java.util.List;

/**
 * TherapistPatient ilişki yönetimi için service interface
 * - Danışan atama işlemleri (BPMN workflow)
 * - Assignment durum yönetimi
 * - Seans takibi (Normalized TherapySession architecture ile)
 * - İlişki istatistikleri (Calculated metodlar)
 *
 * NOT: Session management artık TherapySessionService'te
 */
public interface TherapistPatientService {

    /**
     * BPMN sürecinden gelen assignment request'i işleme
     * (Tek assignment yöntemi - therapist onayı BPMN'de)
     */
    AssignResponse processAssignmentFromBpmn(BpmnAssignmentRequest bpmnRequest) throws ResourceNotFoundException;

    /**
     * Danışman atamasını iptal etme
     */
    void unassignTherapistFromPatient(Long therapistId, Long patientId) throws ResourceNotFoundException;

    /**
     * Belirli danışmanın tüm danışanlarını getirme
     */
    List<PatientSummaryDto> getTherapistPatients(Long therapistId, int page, int size) throws ResourceNotFoundException;

    /**
     * Belirli danışanın danışman bilgisini getirme
     */
    PatientSummaryDto getPatientSummary(Long therapistId, Long patientId) throws ResourceNotFoundException;

    /**
     * Seans tamamlama işlemi (Legacy appointment sistemi)
     * NOT: Yeni projeler için TherapySessionService.completeSession() kullanın
     */
    void completeSession(Long therapistId, Long appointmentId, SessionCompletionRequest request) throws ResourceNotFoundException;

    /**
     * Assignment durum güncelleme
     */
    void updateAssignmentStatus(Long therapistId, Long patientId, String status) throws ResourceNotFoundException;

    /**
     * Danışman-danışan ilişki istatistikleri
     * (Normalized session data'dan calculated)
     */
    AssignmentStatistics getAssignmentStatistics(Long therapistId) throws ResourceNotFoundException;

    /**
     * Aktif atama kontrolü
     */
    boolean hasActiveAssignment(Long therapistId, Long patientId);

    /**
     * Assignment geçmişi
     */
    List<PatientSummaryDto> getAssignmentHistory(Long therapistId, int page, int size) throws ResourceNotFoundException;

    /**
     * Assignment priortiy güncelleme
     */
    void updateAssignmentPriority(Long therapistId, Long patientId, Integer priority) throws ResourceNotFoundException;

    /**
     * Assignment istatistikleri DTO
     * (Normalized TherapySession verilerinden hesaplanır)
     */
    record AssignmentStatistics(
            int totalActiveAssignments,
            int totalCompletedAssignments,
            int totalCancelledAssignments,
            double averageCompletionRate,
            double averageAttendanceRate,
            int totalSessionsCompleted,    // TherapistPatient.getSessionsCompleted()
            int totalSessionsCancelled     // TherapistPatient.getSessionsCancelled()
    ) {}
}