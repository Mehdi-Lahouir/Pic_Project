package com.pic.pic.controller;

import com.pic.pic.service.PunchStreamService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events/{eventId}/punches")
public class PunchStreamController {

    private final PunchStreamService service;

    public PunchStreamController(PunchStreamService service) {
        this.service = service;
    }

    @GetMapping("/stream")
    public SseEmitter stream(@PathVariable long eventId) {
        return service.subscribe(eventId);
    }
}
