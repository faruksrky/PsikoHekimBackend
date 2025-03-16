package com_psikohekim.psikohekim_appt.service.impl;

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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TherapistServiceImpl implements TherapistService {

    private final TherapistRepository therapistRepository;
    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;


    @Override
    public TherapistResponse addTherapist(TherapistRequest therapistReq) throws ConflictException, InvalidRequestException {

        validateTherapistRequest(therapistReq);
        Therapist therapist = modelMapper.map(therapistReq, Therapist.class);
        therapist.setTherapistRating(calculateInitialRating(therapistReq.getYearsOfExperience()));

        try {
            Therapist savedTherapist = therapistRepository.save(therapist);
            return modelMapper.map(savedTherapist, TherapistResponse.class);
        } catch (Exception e) {
            throw new ConflictException("", "Danışman eklenirken bir hata oluştu!");
        }
    }

    @Override
    public Map<String, List<TherapistResponse>> getTherapists() throws ResourceNotFoundException {
        List<Therapist> therapists = therapistRepository.findAll();
        if (therapists.isEmpty()) {
            throw new ResourceNotFoundException("Danışman bulunamadı!");
        }

        List<TherapistResponse> therapistResponses = therapists.stream()
                .map(therapist -> modelMapper.map(therapist, TherapistResponse.class))
                .collect(Collectors.toList());
        Map<String, List<TherapistResponse>> response = new HashMap<>();
        response.put("therapists", therapistResponses);
        return response;
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
    public List<PatientResponse> getPatientsByTherapistId(Long therapistId) {
        Optional<Therapist> therapist = therapistRepository.findById(therapistId);
        return therapist.map(t -> t.getPatients().stream()
                        .map(patient -> modelMapper.map(patient, PatientResponse.class))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public Therapist findByEmail(String email) {
        return therapistRepository.findByTherapistEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Lütfen önce bir terapist seçiniz"));
    }

    private void validateTherapistRequest(TherapistRequest therapistReq) throws InvalidRequestException, ConflictException {
        if (therapistRepository.existsByTherapistEmail(therapistReq.getTherapistEmail())) {
            throw new ConflictException("", therapistReq.getTherapistEmail() + " e-posta adresi ile kayıtlı bir terapist zaten var!");
        }

        Map<String, String> fields = new HashMap<>();
        fields.put("Ad", therapistReq.getTherapistFirstName());
        fields.put("Soyad", therapistReq.getTherapistSurname());
        fields.put("Email", therapistReq.getTherapistEmail());
        fields.put("Telefon Numarası", therapistReq.getTherapistPhoneNumber());
        fields.put("Danışman Türü", String.valueOf(therapistReq.getTherapistType()));
        fields.put("Uzmanlık Alanı", String.valueOf(therapistReq.getSpecializationAreas()));
        fields.put("Deneyim", String.valueOf(therapistReq.getYearsOfExperience()));
        fields.put("Eğitim", therapistReq.getTherapistEducation());
        fields.put("Randevu Ücreti", String.valueOf(therapistReq.getAppointmentFee()));

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                throw new InvalidRequestException(entry.getKey(), entry.getKey() + " boş olamaz!");
            }
        }
    }

    @Override
    public AssignResponse assignPatientToTherapist(Long therapistId, Long patientId) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new IllegalArgumentException("Danışman bulunamadı!"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Hasta bulunamadı!"));

        // Terapist ve hasta ilişkilendirme
        if (!therapist.getPatients().contains(patient)) {
            therapist.getPatients().add(patient);
            patient.getTherapists().add(therapist);

            therapistRepository.save(therapist);
            return new AssignResponse(true, "Terapist ve hasta başarıyla ilişkilendirildi.");

        }
        return new AssignResponse(false, "Hasta zaten bu terapiste atanmış!");
    }

    private int calculateInitialRating(String yearsOfExperience) {
        try {
            Experience experienceLevel = Experience.fromString(yearsOfExperience);
            switch (experienceLevel) {
                case ZERO_TO_ONE:
                    return 10; // Yeni danışman
                case TWO_TO_FIVE:
                    return 20; // Orta düzey
                case SIX_TO_TEN:
                    return 30; // Tecrübeli danışman
                case ELEVEN_TO_FIFTEEN:
                    return 40; // Çok tecrübeli danışman
                case SIXTEEN_TO_TWENTY:
                    return 50; // Deneyimli danışman
                case TWENTY_TO_PLUS:
                    return 75; // Çok deneyimli danışman
                default:
                    return 30; // Varsayılan
            }
        } catch (IllegalArgumentException e) {
            // Geçersiz bir değer geldiğinde varsayılan puan
            return 30;
        }
    }
}
