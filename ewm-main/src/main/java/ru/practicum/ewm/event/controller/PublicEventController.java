package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> findAllPublicEvents(@RequestParam(required = false) String text,
                                                   @RequestParam(required = false) List<Long> categories,
                                                   @RequestParam(required = false) Boolean paid,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                   LocalDateTime rangeStart,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                   LocalDateTime rangeEnd,
                                                   @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   HttpServletRequest request) {
        List<EventShortDto> events = eventService.findAllPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        log.info("GET:/events; text: {}, categories: {}, paid: {}, rangeStart: {}, " +
                "rangeEnd: {}, onlyAvailable: {}, from: {}, size: {}.", text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size);
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        statsClient.save(EndpointHitDto.builder()
                .app("ewm-main")
                .uri(path)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());
        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto findPublicEventById(@PathVariable("id") long id, HttpServletRequest request) {
        EventFullDto event = eventService.findPublicEventById(id);
        log.info("GET: /events/{}", id);
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        statsClient.save(EndpointHitDto.builder()
                .app("ewm-main")
                .uri(path)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());
        return event;
    }
}