package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.PsychologistRequest;
import com_psikohekim.psikohekim_appt.model.Psychologist;
import com_psikohekim.psikohekim_appt.repository.PsychologistRepository;
import com_psikohekim.psikohekim_appt.service.PsychologistService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PsychologistServiceImpl implements PsychologistService {

    private final PsychologistRepository psychologistRepository;

    public void addPsycologist(PsychologistRequest psychologistReq) {
        Psychologist psychologist = new Psychologist();
        psychologist.setPsychologistName(psychologistReq.getName());
        psychologist.setPsychologistSurname(psychologistReq.getSurname());
        psychologist.setPsychologistEmail(psychologistReq.getEmail());
        psychologist.setPsychologistPhone(psychologistReq.getPhoneNumber());
        psychologist.setPsychologistAddress(psychologistReq.getAddress());
        psychologistRepository.save(psychologist);
    }
}
