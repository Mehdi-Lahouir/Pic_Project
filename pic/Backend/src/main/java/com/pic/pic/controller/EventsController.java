package com.pic.pic.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pic.pic.sportident.SportIdentClient;

@RestController
@RequestMapping("/api")
public class EventsController {

    private final SportIdentClient client;

    public EventsController(SportIdentClient client) {
        this.client = client;
    }

    @GetMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public String events() {
        return client.fetchEventsRawJson();
    }
}
