package ru.practicum.ewm.request.dto;

import lombok.*;
import ru.practicum.ewm.request.model.Status;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {
    private Set<Long> requestIds;
    private Status status;
}