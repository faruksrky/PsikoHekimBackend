package com_psikohekim.psikohekim_appt.dto.request;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishMessageRequest {
    private String messageName;
    private String correlationKey;
    private Map<String, Object> variables;
}