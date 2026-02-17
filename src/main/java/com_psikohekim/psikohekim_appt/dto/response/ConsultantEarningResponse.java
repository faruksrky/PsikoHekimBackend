package com_psikohekim.psikohekim_appt.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultantEarningResponse {
    private Long sessionId;
    private Long consultantId;
    private BigDecimal consultantFee;
    private String payoutStatus;
}
