package com_psikohekim.psikohekim_appt.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSessionPriceResponse {
    private Long sessionId;
    private Long clientId;
    private BigDecimal sessionPrice;
    private String currency;
    private String paymentStatus;
}
