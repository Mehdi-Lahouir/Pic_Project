package com.pic.pic.service;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.pic.pic.sportident.SportIdentClient;
import com.pic.pic.sportident.dto.PunchDto;

@Service
public class PunchStreamService {

    private final SportIdentClient client;

    // eventId -> list of subscribers
    private final ConcurrentMap<Long, CopyOnWriteArrayList<SseEmitter>> subs = new ConcurrentHashMap<>();

    // eventId -> last seen punch id (for polling)
    private final ConcurrentMap<Long, Long> afterIdByEvent = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneOffset.UTC);

    public PunchStreamService(SportIdentClient client) {
        this.client = client;
    }

    // REST: initial history
    public List<PunchDto> fetchOnce(long eventId, long afterId) {
        return client.fetchPunches(eventId, afterId);
    }

    // SSE: live
    public SseEmitter subscribe(long eventId, long afterId) {
        SseEmitter emitter = new SseEmitter(0L);

        subs.computeIfAbsent(eventId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // never go backwards (important if other clients already advanced)
        afterIdByEvent.merge(eventId, afterId, Math::max);

        Runnable cleanup = () -> subs.getOrDefault(eventId, new CopyOnWriteArrayList<>()).remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // small hello event (useful for debugging)
        try {
            emitter.send(SseEmitter.event().name("ready").data(new ReadyOut(eventId, afterIdByEvent.get(eventId))));
        } catch (IOException ignored) {}

        return emitter;
    }

    // poll SportIdent every 1s (change to 250ms if you want less delay)
    @Scheduled(fixedDelay = 250)
    public void poll() {
        for (var entry : subs.entrySet()) {
            long eventId = entry.getKey();
            var emitters = entry.getValue();
            if (emitters.isEmpty()) continue;

            long afterId = afterIdByEvent.getOrDefault(eventId, 0L);

            List<PunchDto> punches;
            try {
                punches = client.fetchPunches(eventId, afterId);
            } catch (Exception e) {
                continue;
            }

            long max = afterId;
            for (PunchDto p : punches) {
                max = Math.max(max, p.id());
                String timeText = TIME_FMT.format(Instant.ofEpochMilli(p.time()));
                broadcast(eventId, new PunchOut(p.id(), p.card(), p.code(), p.modem(), p.time(), timeText));
            }
            afterIdByEvent.put(eventId, max);
        }
    }

    // heartbeat so connections stay alive
    @Scheduled(fixedDelay = 1500)
    public void heartbeat() {
        for (var entry : subs.entrySet()) {
            long eventId = entry.getKey();
            var emitters = entry.getValue();
            if (emitters.isEmpty()) continue;

            for (SseEmitter e : emitters) {
                try {
                    e.send(SseEmitter.event().name("keepalive").data(Instant.now().toString()));
                } catch (IOException ex) {
                    emitters.remove(e);
                }
            }
        }
    }
    

    private void broadcast(long eventId, PunchOut payload) {
        var emitters = subs.getOrDefault(eventId, new CopyOnWriteArrayList<>());
        for (SseEmitter e : emitters) {
            try {
                    e.send(SseEmitter.event()
                    .name("punch")
                    .id(String.valueOf(payload.id()))
                    .reconnectTime(1000)
                    .data(payload)
                );
            } catch (IOException ex) {
                emitters.remove(e);
            }
        }
    }

    public record PunchOut(long id, String card, int code, String modem, long time, String timeText) {}
    public record ReadyOut(long eventId, long afterId) {}
}
