package com_psikohekim.psikohekim_appt.exception;


import com_psikohekim.psikohekim_appt.dto.response.CalendarErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class CalendarExceptionHandler {

    @ExceptionHandler(CustomExceptionHandler.CalendarAuthException.class)
    public ResponseEntity<CalendarErrorResponse> handleCalendarAuthException(CustomExceptionHandler.CalendarAuthException ex) {
        CalendarErrorResponse errorResponse = new CalendarErrorResponse(
                ex.getMessage(),
                ex.getErrorCode(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(errorResponse);
    }
}

