package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.AssignTherapistRequest;
import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.request.SessionCompletionRequest;
import com_psikohekim.psikohekim_appt.dto.response.AssignResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistDashboardResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientSummaryDto;
import com_psikohekim.psikohekim_appt.dto.response.TherapistStatistics;
import com_psikohekim.psikohekim_appt.enums.Experience;
import com_psikohekim.psikohekim_appt.enums.PsychiatryAreas;
import com_psikohekim.psikohekim_appt.enums.PsychologistAreas;
import com_psikohekim.psikohekim_appt.enums.AppointmentStatus;
import com_psikohekim.psikohekim_appt.enums.AssignmentStatus;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.model.Appointment;
import com_psikohekim.psikohekim_appt.model.Payment;
import com_psikohekim.psikohekim_appt.model.TherapistPatient;
import com_psikohekim.psikohekim_appt.repository.PatientRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistPatientRepository;
import com_psikohekim.psikohekim_appt.repository.AppointmentRepository;
import com_psikohekim.psikohekim_appt.repository.PaymentRepository;
import com_psikohekim.psikohekim_appt.service.TherapistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TherapistServiceImpl implements TherapistService {

    private final TherapistRepository therapistRepository;
    private final PatientRepository patientRepository;
    private final TherapistPatientRepository therapistPatientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public TherapistResponse addTherapist(TherapistRequest therapistReq) throws ConflictException {
        // 1. Validasyon
        validateTherapistRequest(therapistReq);
        // 2. ModelMapper konfigürasyonu
        configureModelMapper();
        // 3. Dönüşüm ve rating hesaplama
        Therapist therapist = convertToEntity(therapistReq);
        therapist.setTherapistRating(calculateInitialRating(therapistReq.getTherapistYearsOfExperience()));

        // 4. Kaydetme ve response dönüşümü
        return saveAndConvertToResponse(therapist);
    }

    @Override
    public Map<String, List<TherapistResponse>> getTherapists() {
        List<Therapist> therapists = therapistRepository.findAll();
        if (therapists.isEmpty()) {
            throw new ResourceNotFoundException("Danışman bulunamadı!");
        }

        List<TherapistResponse> responses = therapists.stream()
                .map(therapist -> modelMapper.map(therapist, TherapistResponse.class))
                .toList();

        return Collections.singletonMap("therapists", responses);
    }

    @Override
    public List<String> getPyschologistAreas() {
        return Arrays.stream(PsychologistAreas.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getPsychiatristAreas() {
        return Arrays.stream(PsychiatryAreas.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }


    @Override
    public Therapist findByEmail(String email) {
        return therapistRepository.findByTherapistEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Lütfen önce bir Danışman seçiniz"));
    }


    private void configureModelMapper() {
        modelMapper.typeMap(TherapistRequest.class, Therapist.class)
                .addMappings(mapper -> {
                    mapper.using(ctx -> {
                        String value = (String) ctx.getSource();
                        return value != null ? Experience.fromString(value) : null;
                    }).map(TherapistRequest::getTherapistYearsOfExperience, Therapist::setTherapistYearsOfExperience);
                });
    }

    private Therapist convertToEntity(TherapistRequest request) {
        return modelMapper.map(request, Therapist.class);
    }

    private TherapistResponse saveAndConvertToResponse(Therapist therapist) throws ConflictException {
        try {
            Therapist savedTherapist = therapistRepository.save(therapist);
            return modelMapper.map(savedTherapist, TherapistResponse.class);
        } catch (Exception e) {
            log.error("Therapist creation failed: {}", e.getMessage());
            throw new ConflictException("", "Danışman eklenirken bir hata oluştu!");
        }
    }

    private void validateTherapistRequest(TherapistRequest therapistReq) throws ConflictException {
        if (therapistRepository.existsByTherapistEmail(therapistReq.getTherapistEmail())) {
            throw new ConflictException("", therapistReq.getTherapistEmail() + " e-posta adresi ile kayıtlı bir terapist zaten var!");
        }

        Map<String, String> fields = Map.of(
                "Ad", therapistReq.getTherapistFirstName(),
                "Soyad", therapistReq.getTherapistLastName(),
                "Email", therapistReq.getTherapistEmail(),
                "Telefon Numarası", therapistReq.getTherapistPhoneNumber(),
                "Danışman Türü", String.valueOf(therapistReq.getTherapistType()),
                "Uzmanlık Alanı", String.valueOf(therapistReq.getTherapistSpecializationAreas()),
                "Deneyim", String.valueOf(therapistReq.getTherapistYearsOfExperience()),
                "Eğitim", therapistReq.getTherapistEducation(),
                "Randevu Ücreti", String.valueOf(therapistReq.getTherapistAppointmentFee())
        );

        fields.forEach((fieldName, value) -> {
            if (value == null || value.isEmpty()) {
                try {
                    throw new InvalidRequestException(fieldName, fieldName + " boş olamaz!");
                } catch (InvalidRequestException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private Therapist getTherapistById(Long therapistId) {
        return therapistRepository.findById(therapistId)
                .orElseThrow(() -> new IllegalArgumentException("Danışman bulunamadı!"));
    }

    private Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Hasta bulunamadı!"));
    }

    private int calculateInitialRating(String yearsOfExperience) {
        try {
            Experience experienceLevel = Experience.fromString(yearsOfExperience);
            return switch (experienceLevel) {
                case ZERO_TO_ONE -> 10;  // Yeni danışman
                case TWO_TO_FIVE -> 20;  // Orta düzey
                case SIX_TO_TEN -> 30;   // Tecrübeli danışman
                case ELEVEN_TO_FIFTEEN -> 40;  // Çok tecrübeli danışman
                case SIXTEEN_TO_TWENTY -> 50;  // Deneyimli danışman
                case TWENTY_TO_PLUS -> 75;     // Çok deneyimli danışman
            };
        } catch (IllegalArgumentException e) {
            log.warn("Invalid experience level: {}", yearsOfExperience);
            return 30; // Varsayılan puan
        }
    }

    // ========== YENİ DASHBOARD METODLArI ==========
    
    @Override
    @Transactional(readOnly = true)
    public TherapistDashboardResponse getTherapistDashboard(Long therapistId) throws ResourceNotFoundException {
        log.info("Terapist {} için dashboard bilgileri getiriliyor", therapistId);
        
        Therapist therapist = getTherapistById(therapistId);
        
        // Temel istatistikler
        TherapistStatistics stats = getTherapistStatistics(therapistId, "WEEKLY");
        
        // Bugünkü randevular
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        List<Appointment> todayAppointments = appointmentRepository
            .findByTherapistIdAndStartTimeBetween(therapistId, todayStart, todayEnd);
            
        // Bu haftaki randevular  
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime weekEnd = LocalDate.now().with(DayOfWeek.SUNDAY).atTime(23, 59, 59);
        List<Appointment> weekAppointments = appointmentRepository
            .findByTherapistIdAndStartTimeBetween(therapistId, weekStart, weekEnd);
            
        // Son hastalar
        List<PatientSummaryDto> recentPatients = getTherapistPatients(therapistId, 0, 5);
        
        // Bekleyen ödemeler
        List<Payment> pendingPayments = paymentRepository.findByPaymentStatus("PENDING");
        
        return TherapistDashboardResponse.builder()
            .therapistInfo(buildTherapistBasicInfo(therapist))
            .statistics(stats)
            .todayAppointments(convertToAppointmentSummary(todayAppointments))
            .weeklyAppointments(convertToAppointmentSummary(weekAppointments))
            .recentPatients(recentPatients)
            .pendingPayments(convertToPaymentSummary(pendingPayments))
            .generatedAt(LocalDateTime.now())
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryDto> getTherapistPatients(Long therapistId, int page, int size) throws ResourceNotFoundException {
        log.info("Terapist {} için hastalar getiriliyor (sayfa: {}, boyut: {})", therapistId, page, size);
        
        Therapist therapist = getTherapistById(therapistId);
        Pageable pageable = PageRequest.of(page, size);
        
        // TherapistPatient ilişkilerini getir
        List<TherapistPatient> assignments = therapistPatientRepository.findByTherapistId(therapistId);
            
        return assignments.stream()
            .map(this::convertToPatientSummary)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public TherapistStatistics getTherapistStatistics(Long therapistId, String period) throws ResourceNotFoundException {
        log.info("Terapist {} için istatistikler hesaplanıyor (dönem: {})", therapistId, period);
        
        Therapist therapist = getTherapistById(therapistId);
        
        // Temel istatistikler
        List<TherapistPatient> allAssignments = therapistPatientRepository.findByTherapistId(therapistId);
            
        List<Appointment> allAppointments = appointmentRepository.findByTherapistId(therapistId);
        List<Payment> allPayments = paymentRepository.findAll()
            .stream()
            .filter(p -> allAssignments.stream()
                .anyMatch(tp -> tp.getPatient().getPatientID().equals(p.getPatient().getPatientID())))
            .collect(Collectors.toList());
        
        // Bugün için randevular
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        long todayAppointmentCount = allAppointments.stream()
            .filter(a -> a.getStartTime().isAfter(todayStart) && a.getStartTime().isBefore(todayEnd))
            .count();
            
        // Haftalık gelir
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime weekEnd = LocalDate.now().with(DayOfWeek.SUNDAY).atTime(23, 59, 59);
        BigDecimal weeklyRevenue = allPayments.stream()
            .filter(p -> p.getPaymentDate().isAfter(weekStart) && p.getPaymentDate().isBefore(weekEnd))
            .filter(p -> "PAID".equals(p.getPaymentStatus()))
            .map(Payment::getAmountDoctor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return TherapistStatistics.builder()
            .totalActivePatients(allAssignments.size())
            .todayAppointmentCount((int) todayAppointmentCount)
            .totalAppointments(allAppointments.size())
            .weeklyRevenue(weeklyRevenue)
            .lastUpdated(LocalDateTime.now())
            .calculationPeriod(period)
            .build();
    }
    
    @Override
    @Transactional
    public void completeSession(Long therapistId, Long appointmentId, SessionCompletionRequest request) throws ResourceNotFoundException {
        log.info("Terapist {} randevu {} tamamlanıyor", therapistId, appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı"));
            
        if (!appointment.getTherapistId().equals(therapistId)) {
            throw new ResourceNotFoundException("Bu randevu size ait değil");
        }
        
        // Randevuyu tamamla
        appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);
        appointment.setNotes(request.getSessionNotes());
        appointmentRepository.save(appointment);
        
        // Ödeme kaydı oluştur
        if (request.getPaymentReceived()) {
            createPaymentRecord(appointment, request);
        }
        
        log.info("Seans başarıyla tamamlandı");
    }
    
    @Override
    @Transactional(readOnly = true)
    public PatientSummaryDto getPatientSummary(Long therapistId, Long patientId) throws ResourceNotFoundException {
        log.info("Terapist {} için hasta {} özeti getiriliyor", therapistId, patientId);
        
        TherapistPatient assignment = therapistPatientRepository
            .findByTherapistIdAndPatientId(therapistId, patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Hasta ataması bulunamadı"));
            
        return convertToPatientSummary(assignment);
    }
    
    // ========== HELPER METHODS ==========
    
    private TherapistDashboardResponse.TherapistBasicInfo buildTherapistBasicInfo(Therapist therapist) {
        return TherapistDashboardResponse.TherapistBasicInfo.builder()
            .therapistId(therapist.getTherapistId())
            .fullName(therapist.getTherapistFirstName() + " " + therapist.getTherapistLastName())
            .email(therapist.getTherapistEmail())
            .specialization(String.join(", ", therapist.getTherapistSpecializationAreas()))
            .therapistType(therapist.getTherapistType().name())
            .rating(therapist.getTherapistRating())
            .build();
    }
    
    private List<TherapistDashboardResponse.AppointmentSummaryDto> convertToAppointmentSummary(List<Appointment> appointments) {
        return appointments.stream()
            .map(appointment -> {
                Patient patient = patientRepository.findById(appointment.getPatientId())
                    .orElse(null);
                    
                return TherapistDashboardResponse.AppointmentSummaryDto.builder()
                    .appointmentId(appointment.getAppointmentId())
                    .patientName(patient != null ? 
                        patient.getPatientFirstName() + " " + patient.getPatientLastName() : "Bilinmiyor")
                    .appointmentTime(appointment.getStartTime())
                    .status(appointment.getAppointmentStatus().name())
                    .notes(appointment.getNotes())
                    .statusColor(getStatusColor(appointment.getAppointmentStatus()))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private List<TherapistDashboardResponse.PaymentSummaryDto> convertToPaymentSummary(List<Payment> payments) {
        return payments.stream()
            .map(payment -> TherapistDashboardResponse.PaymentSummaryDto.builder()
                .paymentId(payment.getPaymentId())
                .patientName(payment.getPatient().getPatientFirstName() + " " + 
                           payment.getPatient().getPatientLastName())
                .amount(payment.getAmountPatient().toString() + " TL")
                .status(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .statusColor(getPaymentStatusColor(payment.getPaymentStatus()))
                .build())
            .collect(Collectors.toList());
    }
    
    private PatientSummaryDto convertToPatientSummary(TherapistPatient assignment) {
        Patient patient = assignment.getPatient();
        
        // Randevu istatistikleri (gerçek veriler varsa)
        List<Appointment> appointments = appointmentRepository
            .findByTherapistIdAndPatientIdOrderByStartTimeDesc(
                assignment.getTherapist().getTherapistId(), 
                patient.getPatientID());
                
        // Entity'den direkt al ya da hesapla
        int totalSessions = assignment.getTotalSessionsPlanned() != null ? 
            assignment.getTotalSessionsPlanned() : appointments.size();
            
        int completedSessions = assignment.getSessionsCompleted() != null ? 
            assignment.getSessionsCompleted() : 
            (int) appointments.stream()
                .filter(a -> a.getAppointmentStatus() == AppointmentStatus.COMPLETED)
                .count();
        
        // Entity'den completion rate'i al
        double completionRate = assignment.getCompletionRate();
        double attendanceRate = assignment.getAttendanceRate();
        
        // Son randevu tarihi - entity'den al
        LocalDateTime lastSessionDate = assignment.getLastSessionDate() != null ? 
            assignment.getLastSessionDate() : 
            appointments.stream()
                .filter(a -> a.getAppointmentStatus() == AppointmentStatus.COMPLETED)
                .map(Appointment::getStartTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
        // Gün farkı hesaplama
        Long daysSinceLastSession = lastSessionDate != null ? 
            ChronoUnit.DAYS.between(lastSessionDate.toLocalDate(), LocalDate.now()) : null;
        
        return PatientSummaryDto.builder()
            .patientId(patient.getPatientID())
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
            .sessionsCancelled(assignment.getSessionsCancelled())
            .completionRate(completionRate)
            .attendanceRate(attendanceRate)
            .lastSessionDate(lastSessionDate)
            .nextAppointmentDate(assignment.getNextAppointmentDate())
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
    
    private String getStatusColor(AppointmentStatus status) {
        return switch (status) {
            case SCHEDULED -> "#007bff";
            case COMPLETED -> "#28a745";
            case CANCELLED -> "#dc3545";
            case NO_SHOW -> "#ffc107";
            default -> "#6c757d";
        };
    }
    
    private String getPaymentStatusColor(String status) {
        return switch (status) {
            case "PAID" -> "#28a745";
            case "PENDING" -> "#ffc107";
            case "FAILED" -> "#dc3545";
            case "REFUNDED" -> "#6c757d";
            default -> "#6c757d";
        };
    }
    
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

}