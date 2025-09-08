package com_psikohekim.psikohekim_appt.service.outlook;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OutlookOAuthService {

    public String getAccessToken() throws IOException {
        // Burada Outlook OAuth API'yi kullanarak token alacaksın.
        return "outlook_access_token"; // Geçici olarak string döndürüyorum
    }
}
