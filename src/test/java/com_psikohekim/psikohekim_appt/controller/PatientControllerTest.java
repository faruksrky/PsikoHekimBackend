package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.PatientRequest;
import com_psikohekim.psikohekim_appt.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
    }

    @Test
    void addPatient_withValidRequest_shouldReturnOk() throws Exception {
        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setPatientFirstName("Alice");
        patientRequest.setPatientLastName("Smith");
        patientRequest.setPatientEmail("alice.smith@example.com");
        patientRequest.setPatientPhoneNumber("987-654-3210");
        patientRequest.setPatientAddress("456 Elm St, Anytown, USA");
        patientRequest.setPatientCountry("USA");
        patientRequest.setPatientGender("Female");
        patientRequest.setTherapistId(2L);

        doNothing().when(patientService).addPatient(patientRequest);

        mockMvc.perform(post("/patient/addPatient")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientFistName\":\"Alice\",\"patientLastName\":\"Smith\",\"patientEmail\":\"alice.smith@example.com\",\"patientPhoneNumber\":\"987-654-3210\",\"patientAddress\":\"456 Elm St, Anytown, USA\",\"patientDateOfBirth\":\"1985-05-15\",\"patientCountry\":\"USA\",\"patientGender\":\"Female\",\"therapistId\":2}"))
                .andExpect(status().isOk());
    }

    @Test
    void addPatient_withInvalidTherapistId_shouldReturnBadRequest() throws Exception {
        PatientRequest patientRequest = new PatientRequest();
        patientRequest.setPatientFirstName("Bob");
        patientRequest.setPatientLastName("Johnson");
        patientRequest.setPatientEmail("bob.johnson@example.com");
        patientRequest.setPatientPhoneNumber("555-123-4567");
        patientRequest.setPatientAddress("789 Oak St, Anytown, USA");
        patientRequest.setPatientCountry("USA");
        patientRequest.setPatientGender("Male");
        patientRequest.setTherapistId(1L);

        doNothing().when(patientService).addPatient(patientRequest);

        mockMvc.perform(post("/patient/addPatient")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientFistName\":\"Bob\",\"patientLastName\":\"Johnson\",\"patientEmail\":\"bob.johnson@example.com\",\"patientPhoneNumber\":\"555-123-4567\",\"patientAddress\":\"789 Oak St, Anytown, USA\",\"patientDateOfBirth\":\"1978-11-23\",\"patientCountry\":\"USA\",\"patientGender\":\"Male\",\"therapistId\":1}"))
                .andExpect(status().isBadRequest());
    }
}