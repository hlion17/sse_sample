package org.example.sample01.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.sample01.model.EventDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPubSubService {

    private final RedisMessageListenerContainer container;
    private final RedisSubscriber subscriber;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void subscribe(String channel) {
        container.addMessageListener(subscriber, ChannelTopic.of(channel));
    }

    public <T> void publish(String channel, EventDTO event) throws JsonProcessingException {
        redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(event));
    }

    public void removeSubscribe(String channel) {
        container.removeMessageListener(subscriber, ChannelTopic.of(channel));
    }
}
