package org.example.sample01.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sample01.model.EventDTO;
import org.example.sample01.repository.EmitterRepository;
import org.example.sample01.repository.RedisEventRepository;
import org.example.sample01.service.RedisPubSubService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SampleController {

    private final EmitterRepository emitterRepository;
    private final RedisEventRepository redisEventRepository;
    private final RedisPubSubService redisPubSubService;

    private final String CHANEL_NAME = "test";

    @GetMapping(value = "/event/emitter/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getEmitter(@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) throws JsonProcessingException {

        log.info("Last-Event-ID: {}", lastEventId);

//        SseEmitter emitter = new SseEmitter(8 * 60 * 60 * 1000L);
        SseEmitter emitter = new SseEmitter(5000L);

        redisPubSubService.subscribe(CHANEL_NAME);

        this.handleUnconsumedEvent(emitter, lastEventId);

        emitterRepository.save(CHANEL_NAME, emitter);

        emitter.onCompletion(() -> {
            redisPubSubService.removeSubscribe(CHANEL_NAME);
        });

        try {
            emitter.send(SseEmitter.event()
                                   .name("connect")
                                   .data("connected")
            );
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    private void handleUnconsumedEvent(SseEmitter emitter, String lastEventId) throws JsonProcessingException {
        List<EventDTO> unconsumedEventList = redisEventRepository.getEventAfter(CHANEL_NAME, lastEventId);
        for (EventDTO eventDTO : unconsumedEventList) {
            log.info("UnConsumed Event: {}", eventDTO.toString());
            try {
                emitter.send(SseEmitter.event()
                                       .id(eventDTO.getId())
                                       .name("testEvent")
                                       .data(eventDTO.getMessage())
                );
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    @PostMapping("/event/emitter/{eventKey}")
    public void sendEvent(@PathVariable("eventKey") String eventKey, @RequestParam String message) throws JsonProcessingException {
        EventDTO event = EventDTO.builder()
                                 .id(UUID.randomUUID().toString())
                                 .message(message)
                                 .build();
        redisEventRepository.saveEvent(eventKey, event);
        redisPubSubService.publish(eventKey, event);
    }

    @DeleteMapping("/events/{eventKey}")
    public void deleteEvent(@PathVariable("eventKey") String eventKey) {
        redisEventRepository.deleteEvent(eventKey);
    }
}
