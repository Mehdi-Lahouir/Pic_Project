package com.pic.pic.sportident;

import com.pic.pic.sportident.dto.PunchDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class SportIdentClient {

    private final RestClient client;
    private final String token;

    public SportIdentClient(
        @Value("${sportident.baseUrl}") String baseUrl,
        @Value("${sportident.token}") String token
    ) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.token = token;
    }

    public String fetchEventsRawJson() {
        return client.get()
            .uri("/api/rest/v1/events")
            .header("Authorization", token) // token already includes "Bearer ..."
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(String.class);
    }

    public List<PunchDto> fetchPunches(long eventId, long afterId) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/rest/v1/events/{eventId}/punches")
                .queryParam("afterId", afterId)
                .queryParam("sort", "id,asc")
                .queryParam("duplicates", "false")
                .queryParam("projection", "simple")
                .queryParam("limit", 50)
                .build(eventId))
            .header("Authorization", token)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(new ParameterizedTypeReference<List<PunchDto>>() {});
    }
}
