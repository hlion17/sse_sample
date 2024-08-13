package org.example.sample01.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sample01.repository.EmitterRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SampleController {

    private final EmitterRepository emitterRepository;

    @GetMapping(value = "/event/emitter/connect/{value}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getEmitter(@PathVariable String value) {
        SseEmitter emitter = new SseEmitter(8 * 60 * 60 * 1000L);

        emitterRepository.save("test", emitter);

        emitter.onCompletion(() -> {
            log.info("SSE connection completed successfully.");
        });

        emitter.onTimeout(() -> {
            log.error("timeout!!!!");
            try {
                emitter.send(SseEmitter.event()
                                       .name("timeout")
                                       .data("Connection timed out. Please reconnect."));
            } catch (IOException e) {
                log.error("SSE connection timed out: {}", e.getMessage());
            }

            emitter.complete();
        });

        emitter.onError((ex) -> {
            log.error("error!!!! {}", ex.getMessage());
            emitter.completeWithError(ex);
        });

        try {
            emitter.send(SseEmitter.event()
                                   .name("connect")
                                   .data("connected")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return emitter;
    }

    @PostMapping("/event/emitter/{eventKey}")
    public void sendEvent(@PathVariable("eventKey") String eventKey, @RequestParam String message) {
        List<SseEmitter> sseEmitters = emitterRepository.get(eventKey);

        for (SseEmitter sseEmitter : sseEmitters) {
            try {
                sseEmitter.send(SseEmitter.event()
                                          .name("test")
                                          .data(message)
                );
            } catch (Exception e) {
                log.error("Exception occur!!!!!!");
                emitterRepository.remove(eventKey, sseEmitter);
            }
        }
    }
}
