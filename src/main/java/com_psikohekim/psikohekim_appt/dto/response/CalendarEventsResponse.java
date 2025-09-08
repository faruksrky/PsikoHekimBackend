package com_psikohekim.psikohekim_appt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventsResponse {
    private List<CalendarEventResponse> events;
    private int totalCount;
    private Map<String, Long> statistics;
}