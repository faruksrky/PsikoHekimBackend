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
import com_psikohekim.psikohekim_appt.repository.PatientRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
import com_psikohekim.psikohekim_appt.service.TherapistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TherapistServiceImpl implements TherapistService {

    private final TherapistRepository therapistRepository;
    private final PatientRepository patientRepository;
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


}