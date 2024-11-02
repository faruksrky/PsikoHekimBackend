package com_psikohekim.psikohekim_appt.service;


import com_psikohekim.psikohekim_appt.dto.PsychologistRequest;
import org.springframework.stereotype.Service;

@Service
public interface PsychologistService {
    public void addPsycologist( PsychologistRequest psychologist);
}
