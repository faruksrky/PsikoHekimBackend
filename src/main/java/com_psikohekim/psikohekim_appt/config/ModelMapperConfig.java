package com_psikohekim.psikohekim_appt.config;

import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.model.Patient;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        return modelMapper;
    }

    @Bean
    public ModelMapper modelMapperPatient() {
        ModelMapper modelMapper = new ModelMapper();

        // Patient -> PatientResponse Converter
        modelMapper.typeMap(Patient.class, PatientResponse.class).addMappings(mapper -> {
            mapper.skip(PatientResponse::setTherapist); // İlk başta atlamasını sağlıyoruz
        });

        modelMapper.addConverter((Converter<Patient, PatientResponse>) context -> {
            Patient source = context.getSource();
            PatientResponse destination = context.getDestination();

            // Eğer therapists null veya boşsa atla
            if (source.getTherapists() != null && !source.getTherapists().isEmpty()) {
                List<TherapistResponse> therapistResponses = source.getTherapists().stream()
                        .map(therapist -> modelMapper.map(therapist, TherapistResponse.class))
                        .toList();
                destination.setTherapist(therapistResponses);
            }

            return destination;
        });

        return modelMapper;
    }

}
