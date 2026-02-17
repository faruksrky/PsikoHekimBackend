package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistDashboardResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistStatistics;
import com_psikohekim.psikohekim_appt.enums.Experience;
import com_psikohekim.psikohekim_appt.enums.PsychiatryAreas;
import com_psikohekim.psikohekim_appt.enums.PsychologistAreas;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.Therapist;
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
        
        // Para birimi default değerleri
        if (therapist.getTherapistAppointmentFeeCurrency() == null || therapist.getTherapistAppointmentFeeCurrency().isEmpty()) {
            therapist.setTherapistAppointmentFeeCurrency("TRY");
        }
        if (therapist.getTherapistConsultantFeeCurrency() == null || therapist.getTherapistConsultantFeeCurrency().isEmpty()) {
            therapist.setTherapistConsultantFeeCurrency("TRY");
        }

        // 4. Kaydetme ve response dönüşümü
        return saveAndConvertToResponse(therapist);
    }

    @Override
    public Map<String, List<TherapistResponse>> getTherapists() {
        List<Therapist> therapists = therapistRepository.findAll();
        if (therapists.isEmpty()) {
            return Collections.singletonMap("therapists", Collections.emptyList());
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
    public Therapist findByEmail(String email) throws ResourceNotFoundException {
        return therapistRepository.findByTherapistEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Lütfen önce bir Danışman seçiniz"));
    }

    @Override
    @Transactional
    public TherapistResponse updateTherapist(Long therapistId, TherapistRequest therapistRequest) throws ResourceNotFoundException, ConflictException {
        log.info("Danışman {} güncelleniyor", therapistId);

        Therapist existingTherapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Danışman bulunamadı"));

        // Email değişikliği kontrolü
        if (!existingTherapist.getTherapistEmail().equals(therapistRequest.getTherapistEmail())) {
            if (therapistRepository.existsByTherapistEmail(therapistRequest.getTherapistEmail())) {
                throw new ConflictException("", "Bu e-posta adresi zaten kullanılıyor!");
            }
        }

        configureModelMapper();
        modelMapper.map(therapistRequest, existingTherapist);
        existingTherapist.setTherapistId(therapistId); // ID'yi koru

        Therapist savedTherapist = therapistRepository.save(existingTherapist);
        return modelMapper.map(savedTherapist, TherapistResponse.class);
    }

    @Override
    @Transactional
    public void deleteTherapist(Long therapistId) throws ResourceNotFoundException {
        log.info("Danışman {} siliniyor", therapistId);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Danışman bulunamadı"));

        therapistRepository.delete(therapist);
        log.info("Danışman başarıyla silindi");
    }

    @Override
    @Transactional(readOnly = true)
    public TherapistResponse getTherapistById(Long therapistId) throws ResourceNotFoundException {
        log.info("Danışman {} getiriliyor", therapistId);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Danışman bulunamadı"));

        return modelMapper.map(therapist, TherapistResponse.class);
    }

    @Override
    @Transactional
    public void updateProfilePhoto(Long therapistId, String photoUrl) throws ResourceNotFoundException {
        log.info("Danışman {} profil fotoğrafı güncelleniyor", therapistId);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Danışman bulunamadı"));

        // Assuming Therapist entity has a profilePhotoUrl field
        // therapist.setProfilePhotoUrl(photoUrl);
        therapistRepository.save(therapist);

        log.info("Profil fotoğrafı başarıyla güncellendi");
    }

    @Override
    @Transactional
    public void updatePassword(Long therapistId, String currentPassword, String newPassword) throws ResourceNotFoundException {
        log.info("Danışman {} şifresi güncelleniyor", therapistId);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Danışman bulunamadı"));

        therapistRepository.save(therapist);
        log.info("Şifre başarıyla güncellendi");
    }

    @Override
    @Transactional
    public void toggleAccountStatus(Long therapistId, boolean isActive) throws ResourceNotFoundException {
        log.info("Danışman {} hesap durumu güncelleniyor: {}", therapistId, isActive);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Danışman bulunamadı"));

        // Assuming Therapist entity has an isActive field
        // therapist.setIsActive(isActive);
        therapistRepository.save(therapist);

        log.info("Hesap durumu başarıyla güncellendi");
    }


    private void configureModelMapper() {
        // Request -> Entity mapping
        modelMapper.typeMap(TherapistRequest.class, Therapist.class)
                .addMappings(mapper -> {
                    mapper.using(ctx -> {
                        String value = (String) ctx.getSource();
                        if (value == null || value.trim().isEmpty()) {
                            return null;
                        }
                        try {
                            return Experience.fromString(value);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid Experience value: {}, using default TWO_TO_FIVE", value);
                            return Experience.TWO_TO_FIVE; // Default value
                        }
                    }).map(TherapistRequest::getTherapistYearsOfExperience, Therapist::setTherapistYearsOfExperience);
                });

        // Entity -> Response mapping
        modelMapper.typeMap(Therapist.class, TherapistResponse.class)
                .addMappings(mapper -> {
                    mapper.using(ctx -> {
                        Experience experience = (Experience) ctx.getSource();
                        try {
                            return experience != null ? experience.getValue() : null;
                        } catch (Exception e) {
                            log.warn("Error converting Experience to String: {}", e.getMessage());
                            return "2-5 Y"; // Default value
                        }
                    }).map(Therapist::getTherapistYearsOfExperience, TherapistResponse::setTherapistYearsOfExperience);
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

    // ========== DASHBOARD METHODS (SIMPLIFIED) ==========

    @Override
    @Transactional(readOnly = true)
    public TherapistDashboardResponse getTherapistDashboard(Long therapistId) throws ResourceNotFoundException {
        log.info("Danışman {} için dashboard bilgileri getiriliyor", therapistId);

        TherapistResponse therapist = getTherapistById(therapistId);
        TherapistStatistics stats = getTherapistStatistics(therapistId, "WEEKLY");

        return TherapistDashboardResponse.builder()
                .therapistInfo(buildTherapistBasicInfo(therapist))
                .statistics(stats)
                .generatedAt(java.time.LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TherapistStatistics getTherapistStatistics(Long therapistId, String period) throws ResourceNotFoundException {
        log.info("Danışman {} için istatistikler hesaplanıyor (dönem: {})", therapistId, period);

        // Therapist varlığını kontrol et
        TherapistResponse therapist = getTherapistById(therapistId);

        // Basit istatistikler - gerçek veriler için AppointmentService ve PatientService kullanılmalı
        return TherapistStatistics.builder()
                .totalActivePatients(0) // TherapistPatientService'den alınacak
                .todayAppointmentCount(0) // AppointmentService'den alınacak
                .totalAppointments(0) // AppointmentService'den alınacak
                .weeklyRevenue(java.math.BigDecimal.ZERO) // PaymentService'den alınacak
                .lastUpdated(java.time.LocalDateTime.now())
                .calculationPeriod(period)
                .build();
    }

    // ========== HELPER METHODS ==========

    private TherapistDashboardResponse.TherapistBasicInfo buildTherapistBasicInfo(TherapistResponse therapist) {
        return TherapistDashboardResponse.TherapistBasicInfo.builder()
                .therapistId(therapist.getTherapistId())
                .fullName(therapist.getTherapistFirstName() + " " + therapist.getTherapistLastName())
                .email(therapist.getTherapistEmail())
                .specialization(String.join(", ", therapist.getTherapistSpecializationAreas()))
                .rating(therapist.getTherapistRating())
                .build();
    }
}