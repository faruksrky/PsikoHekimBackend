package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.AssignTherapistRequest;
import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.response.AssignResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.enums.Experience;
import com_psikohekim.psikohekim_appt.enums.PsychiatryAreas;
import com_psikohekim.psikohekim_appt.enums.PsychologistAreas;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.model.TherapistAssignment;
import com_psikohekim.psikohekim_appt.repository.PatientRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistAssignmentRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
import com_psikohekim.psikohekim_appt.service.PatientService;
import com_psikohekim.psikohekim_appt.service.TherapistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TherapistServiceImpl implements TherapistService {

    private final TherapistRepository therapistRepository;
    private final PatientRepository patientRepository;
    private final TherapistAssignmentRepository therapistAssignmentRepository;
    private final ModelMapper modelMapper;
    private final PatientService patientService;


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

    @Override
    @Transactional
    public Map<String, Object> sendAssignmentRequest(Map<String, Object> request) throws InvalidRequestException {
        // Validasyon
        if (!request.containsKey("patientId")) {
            throw new InvalidRequestException("patientId", "Danışan ID'si gerekli");
        }
        if (!request.containsKey("therapistId")) {
            throw new InvalidRequestException("therapistId", "Danışman ID'si gerekli");
        }

        String patientId = String.valueOf(request.get("patientId"));
        String therapistId = String.valueOf(request.get("therapistId"));

        // Danışan kontrolü
        PatientResponse patient = patientService.getPatient(Long.valueOf(patientId));
        if (patient == null) {
            throw new ResourceNotFoundException("Danışan bulunamadı: " + patientId);
        }

        // İstek oluşturma
        TherapistAssignment therapistAssignment = createAssignmentRequest(patientId,therapistId);

        // Sonuç döndürme
        return Map.of(
                "requestId", therapistAssignment.getId(),
                "patientId", patientId,
                "patientName", patient.getPatientFirstName(),
                "patientLastName", patient.getPatientLastName(),
                "patientAge", patient.getPatientAge(),
                "status", "pending",
                "requestDate", therapistAssignment.getCreatedAt()
        );
    }

    // Yardımcı metod
    private TherapistAssignment createAssignmentRequest(String patientId, String therapistId) {
        // DTO nesnesi oluşturalım
        AssignTherapistRequest requestDTO = AssignTherapistRequest.builder()
                .patientId(String.valueOf(Long.valueOf(patientId)))
                .therapistId(String.valueOf(Long.valueOf(therapistId)))
                .status("PENDING")
                .build();

        // ModelMapper ile DTO'yu entity'ye dönüştürelim
        TherapistAssignment assignment = modelMapper.map(requestDTO, TherapistAssignment.class);

        // Ek alan ayarlamaları
        assignment.setCreatedAt(LocalDateTime.now());

        // Kaydedelim ve dönelim
        return therapistAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public Map<String, Object> acceptAssignmentRequest(Long requestId) throws InvalidRequestException {
        try {
            // İsteği veritabanından bul
            TherapistAssignment request = therapistAssignmentRepository.findById(requestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Atama isteği bulunamadı: " + requestId));
            // İsteğin durumunu kontrol et
            if (TherapistAssignment.AssignmentStatus.PENDING.equals(request.getStatus())) {
                // İsteği reddet
                request.setStatus(TherapistAssignment.AssignmentStatus.ACCEPTED);
                request.setUpdatedAt(LocalDateTime.now());
                therapistAssignmentRepository.save(request);

                return Map.of(
                        "requestId", request.getId(),
                        "status", "accepted",
                        "message", "Atama isteği kabul edildi"
                );
            } else {
                throw new InvalidRequestException("status", "Bu istek zaten işlenmiş");
            }
        } catch (ResourceNotFoundException | InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Atama isteği reddedilirken bir hata oluştu", e);
        }

    }

    @Override
    @Transactional
    public Map<String, Object> rejectAssignmentRequest(Long requestId) throws InvalidRequestException {
        try {
            // İsteği veritabanından bul
            TherapistAssignment request = therapistAssignmentRepository.findById(requestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Atama isteği bulunamadı: " + requestId));

            // İsteğin durumunu kontrol et
            if (TherapistAssignment.AssignmentStatus.PENDING.equals(request.getStatus())) {
                // İsteği reddet
                request.setStatus(TherapistAssignment.AssignmentStatus.REJECTED);
                request.setUpdatedAt(LocalDateTime.now());
                therapistAssignmentRepository.save(request);

                return Map.of(
                        "requestId", request.getId(),
                        "status", "rejected",
                        "message", "Atama isteği reddedildi"
                );
            } else {
                throw new InvalidRequestException("status", "Bu istek zaten işlenmiş");
            }
        } catch (ResourceNotFoundException | InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Atama isteği reddedilirken bir hata oluştu", e);
        }
    }

    @Override
    public List<Map<String, Object>> getPendingRequests(Long therapistId) {
        try {
            // Repository'den istekleri al
            List<TherapistAssignment> requests = therapistAssignmentRepository.findByTherapistIdAndStatus(
                    String.valueOf(therapistId),
                    TherapistAssignment.AssignmentStatus.PENDING
            ).map(List::of).orElse(Collections.emptyList());

            if (requests.isEmpty()) {
                return Collections.emptyList();
            }

            // Tüm patient ID'leri topla ve String'e çevir
            List<String> patientIds = requests.stream()
                    .map(request -> String.valueOf(request.getPatientId()))
                    .collect(Collectors.toList());

            // Toplu patient bilgilerini al
            Map<String, PatientResponse> patients = patientService.getPatientsByIds(patientIds)
                    .stream()
                    .collect(Collectors.toMap(
                            patient -> String.valueOf(patient.getPatientId()),  // Key'i String'e çevir
                            Function.identity()
                    ));

            return requests.stream()
                    .map(request -> {
                        String patientId = String.valueOf(request.getPatientId());
                        PatientResponse patient = patients.get(patientId);
                        if (patient == null) {
                            log.warn("Patient not found for ID: {}", patientId);
                            return null;
                        }

                        Map<String, Object> response = new HashMap<>();
                        response.put("requestId", request.getId());
                        response.put("patientId", patient.getPatientId());
                        response.put("patientName", patient.getPatientFirstName() + " " + patient.getPatientLastName());
                        response.put("patientAge", patient.getPatientAge());
                        response.put("status", request.getStatus());
                        response.put("requestDate", request.getCreatedAt());
                        return response;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Bekleyen atama istekleri alınırken bir hata oluştu", e);
        }
    }
}