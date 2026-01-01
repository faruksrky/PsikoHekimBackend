package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.PendingRequest;
import com_psikohekim.psikohekim_appt.dto.request.PublishMessageRequest;
import com_psikohekim.psikohekim_appt.dto.response.AssignmentResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.TherapistAssignment;
import com_psikohekim.psikohekim_appt.model.TherapistPatient;
import com_psikohekim.psikohekim_appt.repository.PatientRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistAssignmentRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistPatientRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
import com_psikohekim.psikohekim_appt.service.BpmnServiceClient;
import com_psikohekim.psikohekim_appt.service.PatientService;
import com_psikohekim.psikohekim_appt.service.ProcessService;
import com_psikohekim.psikohekim_appt.service.TherapistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessServiceImpl implements ProcessService {

    private static final String MESSAGE_NAME = "therapist_decision";
    private static final String VARIABLE_DECISION = "TherapistDecision";
    private static final Set<String> REQUIRED_FIELDS = Set.of(
            "patientId", "therapistId", "processName",
            "description", "startedBy", "processInstanceKey"
    );

    private final BpmnServiceClient bpmnServiceClient;
    private final PatientService patientService;
    private final TherapistService therapistService;
    private final TherapistAssignmentRepository therapistAssignmentRepository;
    private final TherapistPatientRepository therapistPatientRepository;
    private final TherapistRepository therapistRepository;
    private final PatientRepository patientRepository;

    @Override
    public Map<String, Object> startTherapistProcess(String businessKey) throws InvalidRequestException {
        if (businessKey == null || businessKey.trim().isEmpty()) {
            throw new InvalidRequestException("Business key must not be null or empty", "");
        }
        return bpmnServiceClient.startProcess(Map.of("businessKey", businessKey));
    }

    @Override
    @Transactional
    public AssignmentResponse sendAssignmentRequest(Map<String, Object> request) throws InvalidRequestException {
        validateAssignmentRequest(request);

        String patientId = String.valueOf(request.get("patientId"));
        String processInstanceKey = String.valueOf(request.get("processInstanceKey"));
        String therapistId = String.valueOf(request.get("therapistId"));
        String processName = String.valueOf(request.get("processName"));
        String description = String.valueOf(request.get("description"));
        String startedBy = String.valueOf(request.get("startedBy"));

        PatientResponse patient = validateAndGetPatient(patientId);

        TherapistAssignment assignment = createAndSaveAssignment(
                patientId, processInstanceKey, therapistId, processName, description, startedBy
        );

        return AssignmentResponse.from(assignment, patient);
    }

    private PatientResponse validateAndGetPatient(String patientId) throws InvalidRequestException {
        try {
            PatientResponse patient = patientService.getPatient(Long.valueOf(patientId));
            if (patient == null) {
                throw new ResourceNotFoundException("Danışan bulunamadı: " + patientId);
            }
            return patient;
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Geçersiz danışan ID formatı: " + patientId, "\"Geçersiz danışan ID formatı:" + patientId);
        }
    }

    private void validateAssignmentRequest(Map<String, Object> request) throws InvalidRequestException {
        List<String> missing = REQUIRED_FIELDS.stream()
                .filter(k -> !request.containsKey(k) ||
                        request.get(k) == null ||
                        String.valueOf(request.get(k)).trim().isEmpty())
                .toList();

        if (!missing.isEmpty()) {
            throw new InvalidRequestException(
                    "Eksik veya geçersiz parametre",
                    "Eksik/geçersiz alanlar: " + String.join(", ", missing)
            );
        }
    }

    private TherapistAssignment createAndSaveAssignment(
            String patientId,
            String processInstanceKey,
            String therapistId,
            String processName,
            String description,
            String startedBy
    ) {
        TherapistAssignment assignment = new TherapistAssignment();
        assignment.setPatientId(patientId);
        assignment.setProcessInstanceKey(processInstanceKey);
        assignment.setTherapistId(therapistId);
        assignment.setProcessName(processName);
        assignment.setDescription(description);
        assignment.setStartedBy(startedBy);
        assignment.setStatus(TherapistAssignment.AssignmentStatus.PENDING);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        return therapistAssignmentRepository.save(assignment);
    }

    @Override
    public List<PendingRequest> getPendingRequests(Long therapistId) throws InvalidRequestException {
        if (therapistId == null) {
            throw new InvalidRequestException("Terapist ID boş olamaz", therapistId.toString());
        }

        List<TherapistAssignment> requests = therapistAssignmentRepository
                .findAllByTherapistId(
                        String.valueOf(therapistId)
                );

        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, PatientResponse> patients = getPatientsMap(requests);
        return createPendingRequestsResponse(requests, patients);
    }

    private Map<Long, PatientResponse> getPatientsMap(List<TherapistAssignment> requests) {
        List<Long> patientIds = requests.stream()
                .map(req -> {
                    try {
                        return Long.parseLong(req.getPatientId());
                    } catch (NumberFormatException e) {
                        log.warn("Invalid patientId format: {}", req.getPatientId());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return patientService.getPatientsByIds(patientIds).stream()
                .collect(Collectors.toMap(
                        PatientResponse::getPatientId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    private List<PendingRequest> createPendingRequestsResponse(
            List<TherapistAssignment> requests,
            Map<Long, PatientResponse> patients
    ) {
        return requests.stream()
                .map(req -> createPendingRequest(req, patients))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private PendingRequest createPendingRequest(
            TherapistAssignment req,
            Map<Long, PatientResponse> patients
    ) {
        try {
            Long patientId = Long.parseLong(req.getPatientId());
            PatientResponse patient = patients.get(patientId);

            if (patient == null) {
                log.warn("No patient data found for ID: {}", patientId);
                return null;
            }

            return PendingRequest.from(req, patient);
        } catch (NumberFormatException e) {
            log.warn("Skipping invalid patientId: {}", req.getPatientId());
            return null;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateAssignmentStatus(String processInstanceKey, String action) throws InvalidRequestException {
        if (processInstanceKey == null || processInstanceKey.trim().isEmpty()) {
            throw new InvalidRequestException("Process instance key boş olamaz", processInstanceKey.toString());
        }

        TherapistAssignment assignment = therapistAssignmentRepository
                .findByProcessInstanceKey(processInstanceKey)
                .orElseThrow(() -> new InvalidRequestException("Geçersiz atama isteği: " + processInstanceKey, ""));

        AssignmentDecision decision = getAssignmentDecision(action);
        updateAssignment(assignment, decision);

        // Accept veya Reject durumuna göre işlem yap
        if (decision.status() == TherapistAssignment.AssignmentStatus.ACCEPTED) {
            handleAcceptance(assignment);
        } else {
            handleRejection(assignment);
        }

        return publishTherapistDecision(assignment, decision.decision());
    }

    private record AssignmentDecision(
            TherapistAssignment.AssignmentStatus status,
            String decision
    ) {}

    private AssignmentDecision getAssignmentDecision(String action) throws InvalidRequestException {
        return switch (action.toLowerCase()) {
            case "accepted" -> new AssignmentDecision(
                    TherapistAssignment.AssignmentStatus.ACCEPTED,
                    "accepted"
            );
            case "rejected" -> new AssignmentDecision(
                    TherapistAssignment.AssignmentStatus.REJECTED,
                    "rejected"
            );
            default -> throw new InvalidRequestException("Geçersiz işlem tipi: " + action, "");
        };
    }

    private void updateAssignment(TherapistAssignment assignment, AssignmentDecision decision) {
        assignment.setStatus(decision.status());
        assignment.setUpdatedAt(LocalDateTime.now());
        therapistAssignmentRepository.save(assignment);
    }

    private void handleAcceptance(TherapistAssignment assignment) {
        try {
            createTherapistPatientRelation(assignment);
            log.info("Therapist-patient relation created for assignment: {}", assignment.getProcessInstanceKey());
        } catch (Exception e) {
            log.error("Error in handleAcceptance: {}", e.getMessage());
            throw new RuntimeException("Terapist atama işlemi başarısız: " + e.getMessage());
        }
    }

    private void handleRejection(TherapistAssignment assignment) {
        try {
            assignment.setRejectionReason("Terapist tarafından reddedildi");
            assignment.setRejectedAt(LocalDateTime.now());
            therapistAssignmentRepository.save(assignment);
            log.info("Assignment rejected: {}", assignment.getProcessInstanceKey());
        } catch (Exception e) {
            log.error("Error in handleRejection: {}", e.getMessage());
            throw new RuntimeException("Red işlemi başarısız: " + e.getMessage());
        }
    }

    private void createTherapistPatientRelation(TherapistAssignment assignment) {
        TherapistPatient relation = new TherapistPatient();
        relation.setTherapist(therapistRepository.findById(Long.valueOf(assignment.getTherapistId()))
                .orElseThrow(() -> new ResourceNotFoundException("Terapist bulunamadı")));
        relation.setPatient(patientRepository.findById(Long.valueOf(assignment.getPatientId()))
                .orElseThrow(() -> new ResourceNotFoundException("Danışan bulunamadı")));
        relation.setAssignedAt(LocalDateTime.now());
        therapistPatientRepository.save(relation);
    }

    private Map<String, Object> publishTherapistDecision(TherapistAssignment assignment, String decision) {
        return publishMessage(PublishMessageRequest.builder()
                .messageName(MESSAGE_NAME)
                .correlationKey(String.valueOf(assignment.getPatientId()))
                .variables(Map.of(VARIABLE_DECISION, decision))
                .build());
    }

    @Override
    public Map<String, Object> publishMessage(PublishMessageRequest request) {
        return bpmnServiceClient.publishMessage(Map.of(
                "messageName", request.getMessageName(),
                "correlationKey", request.getCorrelationKey(),
                "variables", request.getVariables()
        ));
    }

    @Override
    public List<PendingRequest> getIncompleteAssignments() throws InvalidRequestException {
        // PENDING durumundaki tüm atamaları getir
        List<TherapistAssignment> incompleteAssignments = therapistAssignmentRepository
                .findAllByStatus(TherapistAssignment.AssignmentStatus.PENDING);

        if (incompleteAssignments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, PatientResponse> patients = getPatientsMap(incompleteAssignments);
        return createPendingRequestsResponse(incompleteAssignments, patients);
    }

    @Override
    public Map<String, Object> getProcessStatus(String processInstanceKey) throws InvalidRequestException {
        if (processInstanceKey == null || processInstanceKey.trim().isEmpty()) {
            throw new InvalidRequestException("Process instance key boş olamaz", "");
        }

        // Veritabanından atama bilgisini al
        TherapistAssignment assignment = therapistAssignmentRepository
                .findByProcessInstanceKey(processInstanceKey)
                .orElseThrow(() -> new InvalidRequestException("Atama bulunamadı: " + processInstanceKey, ""));

        // Veritabanı verilerine göre süreç adımlarını belirle
        Map<String, Object> processSteps = determineProcessSteps(assignment);
        
        // Danışan ve danışman bilgilerini al
        PatientResponse patient = getPatientById(Long.valueOf(assignment.getPatientId()));
        TherapistResponse therapist = getTherapistById(Long.valueOf(assignment.getTherapistId()));
        
        Map<String, Object> result = new HashMap<>();
        result.put("assignmentId", assignment.getAssignmentId());
        result.put("processInstanceKey", processInstanceKey);
        result.put("status", assignment.getStatus().name());
        result.put("processName", assignment.getProcessName());
        result.put("patientId", assignment.getPatientId());
        result.put("therapistId", assignment.getTherapistId());
        result.put("createdAt", assignment.getCreatedAt());
        result.put("updatedAt", assignment.getUpdatedAt());
        result.put("processSteps", processSteps);
        result.put("currentStep", getCurrentStep(assignment));
        result.put("nextAction", getNextAction(assignment));
        result.put("progressPercentage", getProgressPercentage(assignment));
        
        // Danışan bilgileri
        if (patient != null) {
            result.put("patientName", patient.getPatientFirstName() + " " + patient.getPatientLastName());
            result.put("patientEmail", patient.getPatientEmail());
            result.put("patientPhone", patient.getPatientPhoneNumber());
        } else {
            result.put("patientName", "Bilinmiyor");
            result.put("patientEmail", "");
            result.put("patientPhone", "");
        }
        
        // Danışman bilgileri
        if (therapist != null) {
            result.put("therapistName", therapist.getTherapistFirstName() + " " + therapist.getTherapistLastName());
            result.put("therapistEmail", therapist.getTherapistEmail());
            result.put("therapistPhone", therapist.getTherapistPhoneNumber());
        } else {
            result.put("therapistName", "Bilinmiyor");
            result.put("therapistEmail", "");
            result.put("therapistPhone", "");
        }
        
        return result;
    }

    private Map<String, Object> determineProcessSteps(TherapistAssignment assignment) {
        Map<String, Object> steps = new HashMap<>();
        
        // Adım 1: Süreç Başlatıldı
        steps.put("1_start", Map.of(
            "name", "Süreç Başlatıldı",
            "description", "Danışman atama süreci başlatıldı",
            "status", "completed",
            "completedAt", assignment.getCreatedAt().toString(),
            "icon", "solar:play-circle-bold"
        ));
        
        // Adım 2: İstek Gönderildi
        steps.put("2_send_request", Map.of(
            "name", "İstek Gönderildi",
            "description", "Danışmana atama isteği gönderildi",
            "status", "completed",
            "completedAt", assignment.getCreatedAt().toString(),
            "icon", "solar:letter-unread-bold"
        ));
        
        // Adım 3: Karar Bekleniyor
        String step3Status = assignment.getStatus() == TherapistAssignment.AssignmentStatus.PENDING ? "current" : "completed";
        steps.put("3_wait_decision", Map.of(
            "name", "Karar Bekleniyor",
            "description", "Danışmanın kararı bekleniyor",
            "status", step3Status,
            "completedAt", step3Status.equals("completed") ? assignment.getUpdatedAt().toString() : null,
            "icon", "solar:clock-circle-bold"
        ));
        
        // Adım 4: Karar Kontrolü
        String step4Status = assignment.getStatus() == TherapistAssignment.AssignmentStatus.PENDING ? "pending" : "completed";
        steps.put("4_gateway", Map.of(
            "name", "Karar Kontrolü",
            "description", "Danışman kararı değerlendiriliyor",
            "status", step4Status,
            "completedAt", step4Status.equals("completed") ? assignment.getUpdatedAt().toString() : null,
            "icon", "solar:check-circle-bold"
        ));
        
        // Adım 5: Sonuç
        if (assignment.getStatus() == TherapistAssignment.AssignmentStatus.ACCEPTED) {
            steps.put("5_assign", Map.of(
                "name", "Atama Tamamlandı",
                "description", "Danışman başarıyla atandı",
                "status", "completed",
                "completedAt", assignment.getUpdatedAt().toString(),
                "icon", "solar:user-check-bold"
            ));
            steps.put("5_reject", Map.of(
                "name", "Atama Reddedildi",
                "description", "Bu adım geçildi",
                "status", "skipped",
                "icon", "solar:user-cross-bold"
            ));
        } else if (assignment.getStatus() == TherapistAssignment.AssignmentStatus.REJECTED) {
            steps.put("5_assign", Map.of(
                "name", "Atama Tamamlandı",
                "description", "Bu adım geçildi",
                "status", "skipped",
                "icon", "solar:user-check-bold"
            ));
            steps.put("5_reject", Map.of(
                "name", "Atama Reddedildi",
                "description", "Danışman atamayı reddetti",
                "status", "completed",
                "completedAt", assignment.getUpdatedAt().toString(),
                "icon", "solar:user-cross-bold"
            ));
        } else {
            // PENDING durumunda
            steps.put("5_assign", Map.of(
                "name", "Atama Tamamlandı",
                "description", "Danışman kararını bekliyor",
                "status", "pending",
                "icon", "solar:user-check-bold"
            ));
            steps.put("5_reject", Map.of(
                "name", "Atama Reddedildi",
                "description", "Danışman kararını bekliyor",
                "status", "pending",
                "icon", "solar:user-cross-bold"
            ));
        }
        
        return steps;
    }

    private String getCurrentStep(TherapistAssignment assignment) {
        switch (assignment.getStatus()) {
            case PENDING:
                return "Karar Bekleniyor - Danışman kararını bekliyor";
            case ACCEPTED:
                return "Tamamlandı - Danışman başarıyla atandı";
            case REJECTED:
                return "Tamamlandı - Atama reddedildi";
            default:
                return "Bilinmeyen durum";
        }
    }

    private String getNextAction(TherapistAssignment assignment) {
        switch (assignment.getStatus()) {
            case PENDING:
                return "Danışmanın kararını bekle";
            case ACCEPTED:
                return "Atama tamamlandı, terapi seansları planlanabilir";
            case REJECTED:
                return "Başka bir danışman seçimi yapılabilir";
            default:
                return "Durum kontrol edilmeli";
        }
    }

    private int getProgressPercentage(TherapistAssignment assignment) {
        switch (assignment.getStatus()) {
            case PENDING:
                return 60; // 3/5 adım tamamlandı
            case ACCEPTED:
            case REJECTED:
                return 100; // Tüm adımlar tamamlandı
            default:
                return 0;
        }
    }

    private PatientResponse getPatientById(Long patientId) {
        try {
            return patientService.getPatient(patientId);
        } catch (Exception e) {
            log.warn("Danışan bilgisi alınamadı: {}", e.getMessage());
            return null;
        }
    }

    private TherapistResponse getTherapistById(Long therapistId) {
        try {
            return therapistService.getTherapistById(therapistId);
        } catch (Exception e) {
            log.warn("Danışman bilgisi alınamadı: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> restartAssignment(Long assignmentId) throws InvalidRequestException {
        if (assignmentId == null) {
            throw new InvalidRequestException("Assignment ID boş olamaz", "");
        }

        TherapistAssignment assignment = therapistAssignmentRepository
                .findById(assignmentId)
                .orElseThrow(() -> new InvalidRequestException("Atama bulunamadı: " + assignmentId, ""));

        // Sadece PENDING durumundaki atamalar yeniden başlatılabilir
        if (assignment.getStatus() != TherapistAssignment.AssignmentStatus.PENDING) {
            throw new InvalidRequestException("Sadece bekleyen atamalar yeniden başlatılabilir", "");
        }

        // BPMN'de yeni süreç başlat
        try {
            Map<String, Object> processResult = bpmnServiceClient.startProcess(Map.of(
                "businessKey", "restart-" + assignmentId,
                "patientId", assignment.getPatientId(),
                "therapistId", assignment.getTherapistId(),
                "processName", assignment.getProcessName(),
                "description", "Yeniden başlatılan atama: " + assignment.getDescription(),
                "startedBy", "SYSTEM_RESTART"
            ));

            // Yeni process instance key'i güncelle
            String newProcessInstanceKey = String.valueOf(processResult.get("processInstanceKey"));
            assignment.setProcessInstanceKey(newProcessInstanceKey);
            assignment.setUpdatedAt(LocalDateTime.now());
            therapistAssignmentRepository.save(assignment);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Atama başarıyla yeniden başlatıldı");
            result.put("assignmentId", assignmentId);
            result.put("newProcessInstanceKey", newProcessInstanceKey);
            result.put("processResult", processResult);
            
            return result;
        } catch (Exception e) {
            log.error("Atama yeniden başlatılamadı: {}", e.getMessage());
            throw new InvalidRequestException("Atama yeniden başlatılamadı: " + e.getMessage(), "");
        }
    }
}