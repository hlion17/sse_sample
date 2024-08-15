package org.example.sample01.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sample01.model.EventDTO;
import org.example.sample01.repository.EmitterRepository;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper mapper;
    private final EmitterRepository emitterRepository;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        List<SseEmitter> sseEmitters = emitterRepository.get(channel);
        for (SseEmitter emitter : sseEmitters) {
            try {
                EventDTO eventDTO = mapper.readValue(message.getBody(), EventDTO.class);
                emitter.send(SseEmitter.event()
                                       .id(eventDTO.getId())
                                       .name("testEvent")
                                       .data(eventDTO.getMessage())
                );
            } catch (Exception e) {
                log.error("Exception occur on redis message subscription");
                emitterRepository.remove(channel, emitter);
            }
        }

    }
}
