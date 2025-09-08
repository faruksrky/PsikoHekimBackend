package com_psikohekim.psikohekim_appt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<CustomErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<CustomErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public final ResponseEntity<CustomErrorResponse> handleInvalidRequestException(InvalidRequestException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(ex.getFieldName() + " ", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public final ResponseEntity<CustomErrorResponse> handleConflictException(ConflictException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(ex.getFieldName() + " ", + HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidEventTimeException extends RuntimeException {
        public InvalidEventTimeException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public class CalendarSyncException extends RuntimeException {
        public CalendarSyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class CalendarAuthException extends RuntimeException {
        private final String errorCode;
        private final HttpStatus status;

        public CalendarAuthException(String message, String errorCode) {
            super(message);
            this.errorCode = errorCode;
            this.status = HttpStatus.UNAUTHORIZED;
        }

        public CalendarAuthException(String message, String errorCode, Throwable cause) {
            super(message, cause);
            this.errorCode = errorCode;
            this.status = HttpStatus.UNAUTHORIZED;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }



}