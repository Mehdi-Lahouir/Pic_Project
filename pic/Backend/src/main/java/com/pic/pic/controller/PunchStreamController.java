package com.pic.pic.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.pic.pic.service.PunchStreamService;
import com.pic.pic.sportident.dto.PunchDto;

@RestController
@RequestMapping("/api/events/{eventId}/punches")
public class PunchStreamController {

    private final PunchStreamService service;

    public PunchStreamController(PunchStreamService service) {
        this.service = service;
    }

    @GetMapping
    public List<PunchDto> list(
            @PathVariable long eventId,
            @RequestParam(defaultValue = "0") long afterId
    ) {
        return service.fetchOnce(eventId, afterId);
    }

    @GetMapping("/stream")
    public SseEmitter stream(
            @PathVariable long eventId,
            @RequestParam(defaultValue = "0") long afterId
    ) {
        return service.subscribe(eventId, afterId);
    }
}
