package com_psikohekim.psikohekim_appt.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Session planlama request'i
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionScheduleRequest {

    private Long assignmentId;
    private Long patientId; // Alternative to assignmentId

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledDate;

    private BigDecimal sessionFee;
    private String sessionType; // INITIAL, REGULAR, FOLLOWUP, FINAL
    private String sessionFormat; // IN_PERSON, ONLINE, PHONE
    private String notes;

    // Manual getters (Lombok issue fix)
    public Long getPatientId() {
        return patientId;
    }

    // Validation
    public boolean isValid() {
        return assignmentId != null &&
                scheduledDate != null &&
                scheduledDate.isAfter(LocalDateTime.now().minusHours(1)); // 1 saat tolerans
    }
}