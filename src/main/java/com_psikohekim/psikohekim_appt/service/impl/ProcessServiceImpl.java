package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.PendingRequest;
import com_psikohekim.psikohekim_appt.dto.request.PublishMessageRequest;
import com_psikohekim.psikohekim_appt.dto.response.AssignmentResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
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
}