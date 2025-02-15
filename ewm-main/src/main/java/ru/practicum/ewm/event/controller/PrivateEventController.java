package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> findAllEventsByUserId(@PathVariable("userId") Long userId,
                                                     @RequestParam(defaultValue = "0") int from,
                                                     @RequestParam(defaultValue = "10") int size) {
        log.info("GET:/users/{}/events; from: {}, size: {}.", userId, from, size);
        return eventService.findAllEventsByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@RequestBody @Valid NewEventDto newEventDto, @PathVariable("userId") long userId) {
        log.info("POST:/users/{}/events; newEventDto: {}.", userId, newEventDto);
        return eventService.createEvent(newEventDto, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findEventByUser(@PathVariable("userId") Long userId, @PathVariable("eventId") Long eventId) {
        log.info("GET:/users/{}/events/{}", userId, eventId);
        return eventService.findEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(@PathVariable long userId,
                                          @PathVariable long eventId,
                                          @Valid @RequestBody UpdateEventUserRequest updateEventDto) {
        log.info("PATCH:/users/{}/events/{}; UpdateEventUserRequest: {}. ", userId, eventId, updateEventDto);
        return eventService.updateEventById(userId, eventId, updateEventDto);
    }
}