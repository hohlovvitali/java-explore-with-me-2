package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}")
public class RequestController {
    private final RequestService requestService;

    @GetMapping("/requests")
    public List<ParticipationRequestDto> findAllRequests(@PathVariable("userId") Long userId) {
        log.info("GET:/users/{}/requests", userId);
        return requestService.findAllRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable("userId") Long userId,
                                                 @RequestParam Long eventId) {
        log.info("POST:/users/{}/requests; eventId: {}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable("userId") Long userId,
                                                 @PathVariable("requestId") Long requestId) {
        log.info("POST:/users/{}/requests/{}/cancel", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> findAllRequestsByEvent(@PathVariable("userId") Long userId,
                                                                @PathVariable("eventId") Long eventId) {
        log.info("GET:/users/{}/events/{}/requests", userId, eventId);
        return requestService.findAllRequestsByEvent(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable("userId") Long userId,
                                                         @PathVariable("eventId") Long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("PATCH:/users/{}/events/{}/requests; EventRequestStatusUpdateRequest: {}", userId, eventId, updateRequest);
        return requestService.updateRequests(userId, eventId, updateRequest);
    }
}