package org.example.sample01.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Repository
public class EmitterRepository {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> emittersMap = new ConcurrentHashMap<>();

    public void save(String key, SseEmitter emitter) {
        emittersMap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);

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
    }

    public List<SseEmitter> get(String key) {
        return emittersMap.getOrDefault(key, new CopyOnWriteArrayList<>());
    }

    public void remove(String key, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> sseEmitters = emittersMap.get(key);
        sseEmitters.remove(emitter);
    }

    public void removeAll(String key) {
        emittersMap.remove(key);
    }
}
