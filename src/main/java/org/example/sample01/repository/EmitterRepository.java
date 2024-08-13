package org.example.sample01.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class EmitterRepository {

    private ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> repository = new ConcurrentHashMap<>();

    public void save(String key, SseEmitter emitter) {
        repository.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public List<SseEmitter> get(String key) {
        return repository.getOrDefault(key, new CopyOnWriteArrayList<>());
    }

    public void remove(String key, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> sseEmitters = repository.get(key);
        sseEmitters.remove(emitter);
    }

    public void removeAll(String key) {
        repository.remove(key);
    }
}
