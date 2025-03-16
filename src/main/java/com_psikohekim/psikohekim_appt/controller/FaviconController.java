package com_psikohekim.psikohekim_appt.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@Controller
public class FaviconController {

    @GetMapping("favicon.ico")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnNoFavicon() {
        // Boş 204 yanıtı döndür
    }
}