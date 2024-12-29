package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.service.TherapistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com_psikohekim.psikohekim_appt.enums.TherapistType;
import com_psikohekim.psikohekim_appt.enums.Experience;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TherapistControllerTest {

    @Mock
    private TherapistService therapistService;

    @InjectMocks
    private TherapistController therapistController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(therapistController).build();
    }

    @Test
    void addTherapist() throws Exception {
        TherapistRequest request = new TherapistRequest();
        request.setTherapistFirstName("John");
        request.setTherapistSurname("Doe");
        request.setTherapistEmail("john.doe@example.com");
        request.setTherapistPhoneNumber("1234567890");
        request.setTherapistAddress("123 Main St");
        request.setTherapistType(String.valueOf(TherapistType.PSIKOLOG));
        request.setTherapistEducation("PhD in Psychology");

        mockMvc.perform(post("/therapist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"firstName\": \"John\",\n" +
                        "    \"lastName\": \"Doe\",\n" +
                        "    \"email\": \"doe@example.com\",\n" +
                        "   \"phoneNumber\": \"1234567890\",\n" +
                        "    \"address\": \"123 Main St\",\n" +
                        "    \"therapistType\": \"PSIKOLOG\",\n" +
                        "    \"specializationAreas\": [\"Area1\", \"Area2\"],\n" +
                        "    \"education\": \"PhD in Psychology\",\n" +
                        "    \"appointmentFee\": 100.0,\n" +
                        "    \"certifications\": \"Cert1\",\n" +
                        "    \"university\": \"University1\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(therapistService, times(1)).addTherapist(any(TherapistRequest.class));
    }

    @Test
    void getPsychiatryAreas() throws Exception {
        when(therapistService.getPsychiatristAreas()).thenReturn(Arrays.asList("Area1", "Area2"));

        mockMvc.perform(get("/therapist/psychiatry_areas"))
                .andExpect(status().isOk());

        verify(therapistService, times(1)).getPsychiatristAreas();
    }

    @Test
    void getPsychologistAreas() throws Exception {
        when(therapistService.getPyschologistAreas()).thenReturn(Arrays.asList("Area1", "Area2"));

        mockMvc.perform(get("/therapist/psychologist_areas"))
                .andExpect(status().isOk());

        verify(therapistService, times(1)).getPyschologistAreas();
    }
}