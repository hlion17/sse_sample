package org.example.sample01.model;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class EventDTO implements Serializable {
    private String id;
    private String message;
}
