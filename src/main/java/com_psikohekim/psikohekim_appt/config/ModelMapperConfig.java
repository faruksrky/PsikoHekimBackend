package com_psikohekim.psikohekim_appt.config;

import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.enums.Experience;
import com_psikohekim.psikohekim_appt.model.Patient;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Configuration
@Slf4j
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // String to Experience enum converter
        modelMapper.addConverter((Converter<String, Experience>) ctx -> {
            String source = ctx.getSource();
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            try {
                return Experience.fromString(source);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid Experience value: {}, using default TWO_TO_FIVE", source);
                return Experience.TWO_TO_FIVE;
            }
        });

        // Experience enum to String converter
        modelMapper.addConverter((Converter<Experience, String>) ctx -> {
            Experience source = ctx.getSource();
            try {
                return source != null ? source.getValue() : null;
            } catch (Exception e) {
                log.warn("Error converting Experience to String: {}", e.getMessage());
                return "2-5 Y";
            }
        });

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
            return destination;
        });

        return modelMapper;
    }

}
