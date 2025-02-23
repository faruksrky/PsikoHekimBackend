package com_psikohekim.psikohekim_appt.service.outlook;

import com_psikohekim.psikohekim_appt.model.CalendarEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OutlookCalendarService {

    public List<CalendarEvent> getEvents(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create("https://graph.microsoft.com/v1.0/me/events"))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject jsonResponse = new JSONObject(response.body());
        JSONArray eventsArray = jsonResponse.getJSONArray("value");

        return eventsArray.toList().stream()
                .map(event -> new CalendarEvent(event.toString(), "", "", "", ""))
                .collect(Collectors.toList());
    }
}
