package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.request.TherapistRequest;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
import com_psikohekim.psikohekim_appt.exception.ConflictException;
import com_psikohekim.psikohekim_appt.exception.InvalidRequestException;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TherapistServiceImplTest {

    @Mock
    private TherapistRepository therapistRepository;

    @InjectMocks
    private TherapistServiceImpl therapistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

   @Test
void addTherapistSuccessfully() throws ConflictException, InvalidRequestException {
    TherapistRequest request = new TherapistRequest();
    request.setTherapistFirstName("John");
    request.setTherapistSurname("Doe");
    request.setTherapistEmail("john.doe@example.com");
    request.setTherapistPhoneNumber("1234567890");
    request.setTherapistAddress("123 Main St");
    request.setTherapistEducation("PhD in Psychology");

    request.setSpecializationAreas(Collections.singletonList("Depression, Anxiety, Stress"));

    TherapistResponse therapistResponse = therapistService.addTherapist(request);

    verify(therapistRepository, times(1)).save(any(Therapist.class));
}

    @Test
    void getPyschologistAreas() {
        assertEquals(5, therapistService.getPyschologistAreas().size());
    }

    @Test
    void getPsychiatristAreas() {
        assertEquals(3, therapistService.getPsychiatristAreas().size());
    }

}