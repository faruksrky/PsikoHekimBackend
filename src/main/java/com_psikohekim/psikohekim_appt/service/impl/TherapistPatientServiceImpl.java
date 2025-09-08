package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.BpmnAssignmentRequest;
import com_psikohekim.psikohekim_appt.dto.request.SessionCompletionRequest;
import com_psikohekim.psikohekim_appt.dto.response.AssignResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientSummaryDto;
import com_psikohekim.psikohekim_appt.enums.AppointmentStatus;
import com_psikohekim.psikohekim_appt.enums.AssignmentStatus;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Appointment;
import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.model.Payment;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.model.TherapistPatient;
import com_psikohekim.psikohekim_appt.repository.AppointmentRepository;
import com_psikohekim.psikohekim_appt.repository.PatientRepository;
import com_psikohekim.psikohekim_appt.repository.PaymentRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistPatientRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
import com_psikohekim.psikohekim_appt.service.TherapistPatientService;
import com_psikohekim.psikohekim_appt.service.TherapySessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TherapistPatientServiceImpl implements TherapistPatientService {

    private final TherapistPatientRepository therapistPatientRepository;
    private final TherapistRepository therapistRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final TherapySessionService therapySessionService;

    @Override
    @Transactional
    public AssignResponse processAssignmentFromBpmn(BpmnAssignmentRequest bpmnRequest) throws ResourceNotFoundException {
        log.info("BPMN sürecinden assignment işleniyor: Process={}, Patient={}, Therapist={}",
                bpmnRequest.getProcessId(), bpmnRequest.getPatientId(), bpmnRequest.getTherapistId());

        // BPMN request validasyonu
        if (!bpmnRequest.isValid()) {
            throw new ResourceNotFoundException("BPMN request geçersiz: processId, patientId, therapistId gerekli");
        }

        // Therapist ve Patient bilgilerini al
        Therapist therapist = getTherapistById(bpmnRequest.getTherapistId());
        Patient patient = getPatientById(bpmnRequest.getPatientId());

        // Varolan atama kontrolü
        if (hasActiveAssignment(bpmnRequest.getTherapistId(), bpmnRequest.getPatientId())) {
            throw new ResourceNotFoundException("Bu danışan zaten bu danışmana atanmış!");
        }

        // Assignment entity oluştur
        TherapistPatient assignment = buildAssignmentFromBpmn(bpmnRequest, therapist, patient);
        TherapistPatient savedAssignment = therapistPatientRepository.save(assignment);

        log.info("BPMN assignment başarıyla işlendi: Process={}, AssignmentId={}",
                bpmnRequest.getProcessId(), savedAssignment.getTherapistPatientId());

        // Basit success response döndür
        return new AssignResponse(true, "Therapist onayı ile assignment başarıyla oluşturuldu");
    }

    @Override
    @Transactional
    public void unassignTherapistFromPatient(Long therapistId, Long patientId) throws ResourceNotFoundException {
        log.info("Danışan {} danışman {} ataması iptal ediliyor", patientId, therapistId);

        TherapistPatient assignment = therapistPatientRepository
                .findByTherapistIdAndPatientId(therapistId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Atama bulunamadı"));

        assignment.setAssignmentStatus(AssignmentStatus.CANCELLED);
        assignment.setTreatmentEndDate(LocalDateTime.now());
        therapistPatientRepository.save(assignment);

        log.info("Atama başarıyla iptal edildi");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryDto> getTherapistPatients(Long therapistId, int page, int size) throws ResourceNotFoundException {
        log.info("Danışman {} için hastalar getiriliyor (sayfa: {}, boyut: {})", therapistId, page, size);

        // Therapist varlığını kontrol et
        getTherapistById(therapistId);

        Pageable pageable = PageRequest.of(page, size);
        List<TherapistPatient> assignments = therapistPatientRepository.findByTherapistId(therapistId);

        log.info("Found {} assignments for therapist {}", assignments.size(), therapistId);
        if (!assignments.isEmpty()) {
            log.info("First assignment: id={}, patient={}",
                    assignments.get(0).getTherapistPatientId(),
                    assignments.get(0).getPatient());
        }

        return assignments.stream()
                .map(this::convertToPatientSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PatientSummaryDto getPatientSummary(Long therapistId, Long patientId) throws ResourceNotFoundException {
        log.info("Danışman {} için hasta {} özeti getiriliyor", therapistId, patientId);

        TherapistPatient assignment = therapistPatientRepository
                .findByTherapistIdAndPatientId(therapistId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta ataması bulunamadı"));

        return convertToPatientSummary(assignment);
    }

    @Override
    @Transactional
    public void completeSession(Long therapistId, Long appointmentId, SessionCompletionRequest request) throws ResourceNotFoundException {
        log.info("Danışman {} randevu {} tamamlanıyor", therapistId, appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı"));

        if (!appointment.getTherapistId().equals(therapistId)) {
            throw new ResourceNotFoundException("Bu randevu size ait değil");
        }

        // Randevuyu tamamla (legacy support)
        appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);
        appointment.setNotes(request.getSessionNotes());
        appointmentRepository.save(appointment);

        // TODO: TherapySession oluştur veya güncelle
        // Gelecekte appointment → session migration yaparken bu kısım güncellenecek

        // Ödeme kaydı oluştur
        if (request.getPaymentReceived()) {
            createPaymentRecord(appointment, request);
        }

        log.info("Seans başarıyla tamamlandı - Legacy appointment system kullanıldı");
    }

    @Override
    @Transactional
    public void updateAssignmentStatus(Long therapistId, Long patientId, String status) throws ResourceNotFoundException {
        log.info("Danışman {} hasta {} atama durumu güncelleniyor: {}", therapistId, patientId, status);

        TherapistPatient assignment = therapistPatientRepository
                .findByTherapistIdAndPatientId(therapistId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Atama bulunamadı"));

        AssignmentStatus newStatus = AssignmentStatus.valueOf(status.toUpperCase());
        assignment.setAssignmentStatus(newStatus);

        if (newStatus == AssignmentStatus.COMPLETED || newStatus == AssignmentStatus.CANCELLED) {
            assignment.setTreatmentEndDate(LocalDateTime.now());
        }

        therapistPatientRepository.save(assignment);
        log.info("Atama durumu başarıyla güncellendi");
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentStatistics getAssignmentStatistics(Long therapistId) throws ResourceNotFoundException {
        log.info("Danışman {} için atama istatistikleri hesaplanıyor", therapistId);

        // Therapist varlığını kontrol et
        getTherapistById(therapistId);

        List<TherapistPatient> allAssignments = therapistPatientRepository.findByTherapistId(therapistId);

        int totalActive = (int) allAssignments.stream()
                .filter(a -> a.getAssignmentStatus() == AssignmentStatus.ACTIVE)
                .count();

        int totalCompleted = (int) allAssignments.stream()
                .filter(a -> a.getAssignmentStatus() == AssignmentStatus.COMPLETED)
                .count();

        int totalCancelled = (int) allAssignments.stream()
                .filter(a -> a.getAssignmentStatus() == AssignmentStatus.CANCELLED)
                .count();

        double averageCompletion = allAssignments.stream()
                .mapToDouble(TherapistPatient::getCompletionRate)
                .average()
                .orElse(0.0);

        double averageAttendance = allAssignments.stream()
                .mapToDouble(TherapistPatient::getAttendanceRate)
                .average()
                .orElse(0.0);

        // Calculated metodlar artık null dönmez, int değer döner
        int totalSessionsCompleted = allAssignments.stream()
                .mapToInt(TherapistPatient::getSessionsCompleted)
                .sum();

        int totalSessionsCancelled = allAssignments.stream()
                .mapToInt(TherapistPatient::getSessionsCancelled)
                .sum();

        return new AssignmentStatistics(
                totalActive,
                totalCompleted,
                totalCancelled,
                averageCompletion,
                averageAttendance,
                totalSessionsCompleted,
                totalSessionsCancelled
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveAssignment(Long therapistId, Long patientId) {
        return therapistPatientRepository
                .findByTherapistIdAndPatientId(therapistId, patientId)
                .map(assignment -> assignment.getAssignmentStatus() == AssignmentStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryDto> getAssignmentHistory(Long therapistId, int page, int size) throws ResourceNotFoundException {
        log.info("Danışman {} için atama geçmişi getiriliyor", therapistId);

        // Therapist varlığını kontrol et
        getTherapistById(therapistId);

        Pageable pageable = PageRequest.of(page, size);
        List<TherapistPatient> assignments = therapistPatientRepository.findByTherapistId(therapistId);

        return assignments.stream()
                .filter(a -> a.getAssignmentStatus() != AssignmentStatus.ACTIVE)
                .map(this::convertToPatientSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateAssignmentPriority(Long therapistId, Long patientId, Integer priority) throws ResourceNotFoundException {
        log.info("Danışman {} hasta {} öncelik güncelleniyor: {}", therapistId, patientId, priority);

        TherapistPatient assignment = therapistPatientRepository
                .findByTherapistIdAndPatientId(therapistId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Atama bulunamadı"));

        assignment.setPriorityLevel(priority);
        therapistPatientRepository.save(assignment);

        log.info("Öncelik başarıyla güncellendi");
    }

    // ========== HELPER METHODS ==========

    private Therapist getTherapistById(Long therapistId) {
        return therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Danışman bulunamadı!"));
    }

    private Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı!"));
    }

    private PatientSummaryDto convertToPatientSummary(TherapistPatient assignment) {
        log.info("Converting assignment to PatientSummaryDto: assignmentId={}, patient={}",
                assignment.getTherapistPatientId(), assignment.getPatient());
        Patient patient = assignment.getPatient();

        // ========== NORMALIZED SESSION DATA (NEW) ==========
        // Artık calculated metodları kullanıyoruz, denormalized field'lar yok

        int totalSessions = assignment.getTotalSessionsPlanned() != null ?
                assignment.getTotalSessionsPlanned() : assignment.getTotalSessionsActual();

        // Calculated metodlardan değerleri al
        int completedSessions = assignment.getSessionsCompleted();
        int cancelledSessions = assignment.getSessionsCancelled();
        int scheduledSessions = assignment.getSessionsScheduled();

        double completionRate = assignment.getCompletionRate();
        double attendanceRate = assignment.getAttendanceRate();

        // Calculated tarihler
        LocalDateTime lastSessionDate = assignment.getLastSessionDate();
        LocalDateTime nextAppointmentDate = assignment.getNextAppointmentDate();

        Long daysSinceLastSession = lastSessionDate != null ?
                ChronoUnit.DAYS.between(lastSessionDate.toLocalDate(), LocalDate.now()) : null;
        log.info("Converting assignment: therapistPatientId={}, patientId={}",
                assignment.getTherapistPatientId(), patient.getPatientId());

        return PatientSummaryDto.builder()
                .assignmentId(assignment.getTherapistPatientId())
                .patientId(patient.getPatientId())
                .patientName(patient.getPatientFirstName() + " " + patient.getPatientLastName())
                .patientEmail(patient.getPatientEmail())
                .patientPhone(patient.getPatientPhoneNumber())
                .patientAge(patient.getPatientAge())
                .patientGender(patient.getPatientGender())
                .assignedAt(assignment.getAssignedAt())
                .treatmentStartDate(assignment.getTreatmentStartDate())
                .assignmentStatus(assignment.getAssignmentStatus() != null ?
                        assignment.getAssignmentStatus().name() : "ACTIVE")
                .priorityLevel(assignment.getPriorityLevel())
                .totalSessionsPlanned(totalSessions)
                .sessionsCompleted(completedSessions)
                .sessionsCancelled(cancelledSessions)
                .completionRate(completionRate)
                .attendanceRate(attendanceRate)
                .lastSessionDate(lastSessionDate)
                .nextAppointmentDate(nextAppointmentDate)
                .daysSinceLastSession(daysSinceLastSession)
                .primaryDiagnosis(assignment.getPrimaryDiagnosis())
                .currentStatus(assignment.getAssignmentStatus() != null ?
                        assignment.getAssignmentStatus().getDisplayName() : "Aktif")
                .outstandingBalance(assignment.getOutstandingBalance() != null ?
                        assignment.getOutstandingBalance().toString() + " TL" : "0 TL")
                .hasOutstandingPayments(assignment.getOutstandingBalance() != null &&
                        assignment.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
                .statusColor(getAssignmentStatusColor(assignment.getAssignmentStatus()))
                .priorityText(getPriorityText(assignment.getPriorityLevel()))
                .build();
    }

    // ========== REMOVED: updateAssignmentProgress ==========
    // Bu metod kaldırıldı çünkü artık TherapistPatient entity'sindeki
    // calculated metodlar (getSessionsCompleted(), getCompletionRate(), vb.)
    // session verilerini otomatik olarak hesaplıyor.
    // Manuel field güncellemesi gereksiz hale geldi.

    private void createPaymentRecord(Appointment appointment, SessionCompletionRequest request) {
        Patient patient = patientRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı"));

        Payment payment = new Payment();
        payment.setPatient(patient);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmountPatient(request.getSessionFee());
        payment.setAmountDoctor(request.getSessionFee().multiply(BigDecimal.valueOf(0.8))); // %80 doktor payı
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus("PAID");
        payment.setDescription("Seans ücreti - " + appointment.getAppointmentId());

        paymentRepository.save(payment);
        log.info("Ödeme kaydı oluşturuldu: {}", payment.getPaymentId());
    }

    private String getAssignmentStatusColor(AssignmentStatus status) {
        if (status == null) return "#28a745";
        return switch (status) {
            case ACTIVE -> "#28a745";
            case COMPLETED -> "#007bff";
            case PAUSED -> "#ffc107";
            case CANCELLED -> "#dc3545";
            case ON_HOLD -> "#6c757d";
        };
    }

    private String getPriorityText(Integer priority) {
        if (priority == null) return "Normal";
        return switch (priority) {
            case 1 -> "Düşük";
            case 2 -> "Normal";
            case 3 -> "Orta";
            case 4 -> "Yüksek";
            case 5 -> "Acil";
            default -> "Normal";
        };
    }

    /**
     * BPMN request'ten TherapistPatient entity oluştur
     */
    /**
     * BPMN request'ten TherapistPatient entity oluştur
     * Normalized session architecture ile güncellenmiş versiyon
     */
    private TherapistPatient buildAssignmentFromBpmn(BpmnAssignmentRequest bpmnRequest,
                                                     Therapist therapist, Patient patient) {
        return TherapistPatient.builder()
                .therapist(therapist)
                .patient(patient)
                .assignedAt(LocalDateTime.now())
                .treatmentStartDate(LocalDateTime.now().plusDays(1)) // Yarından başla
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .priorityLevel(2) // Normal priority
                .totalSessionsPlanned(10) // Default session count
                .isActive(true)
                .notes("BPMN Process: " + bpmnRequest.getProcessId() +
                        " - Therapist onayı ile otomatik atama")
                // ========== REMOVED DENORMALIZED FIELDS ==========
                // sessionsCompleted, sessionsCancelled, completionRate,
                // attendanceRate, outstandingBalance artık calculated metodlar
                .build();
    }

    // ========== REMOVED: convertToPatientResponse ==========
    // Bu metod kullanılmıyor, kaldırıldı.
}