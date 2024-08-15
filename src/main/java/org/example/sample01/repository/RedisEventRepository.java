package org.example.sample01.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.sample01.model.EventDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisEventRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveEvent(String eventKey, EventDTO value) throws JsonProcessingException {
        redisTemplate.opsForList().rightPush(eventKey, objectMapper.writeValueAsString(value));
    }

    public List<EventDTO> getEventList(String eventKey) throws JsonProcessingException {
        List<Object> redisValues = redisTemplate.opsForList().range(eventKey, 0, -1);
        if (CollectionUtils.isEmpty(redisValues)) return Collections.emptyList();

        List<EventDTO> eventDataList = new ArrayList<>();
        for (Object redisValue : redisValues) {
            eventDataList.add(objectMapper.readValue((String)redisValue, EventDTO.class));
        }
        return eventDataList;
    }

    public List<EventDTO> getEventAfter(String eventKey, String targetEventId) throws JsonProcessingException {
        List<EventDTO> eventList = this.getEventList(eventKey);
        Optional<EventDTO> targetEventOpt = eventList.stream()
                                                     .filter(e -> e.getId().equals(targetEventId))
                                                     .findFirst();
        if (targetEventOpt.isPresent()) {
            EventDTO eventDTO = targetEventOpt.get();
            int index = eventList.indexOf(eventDTO);
            return (index + 1 == eventList.size()) ? List.of() : eventList.subList(index + 1, eventList.size());
        } else {
            return List.of();
        }
    }

    public void deleteEvent(String eventKey) {
        redisTemplate.delete(eventKey);
    }
}
