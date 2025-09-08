package com_psikohekim.psikohekim_appt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TherapistDashboardResponse {
    
    private TherapistBasicInfo therapistInfo;
    private TherapistStatistics statistics;
    private List<AppointmentSummaryDto> todayAppointments;
    private List<AppointmentSummaryDto> weeklyAppointments;
    private List<PatientSummaryDto> recentPatients;
    private List<PaymentSummaryDto> pendingPayments;
    private LocalDateTime generatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TherapistBasicInfo {
        private Long therapistId;
        private String fullName;
        private String email;
        private String specialization;
        private String therapistType;
        private Integer rating;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentSummaryDto {
        private Long appointmentId;
        private String patientName;
        private LocalDateTime appointmentTime;
        private String status;
        private String notes;
        private String statusColor;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummaryDto {
        private Long paymentId;
        private String patientName;
        private String amount;
        private String status;
        private LocalDateTime paymentDate;
        private String statusColor;
    }
} 