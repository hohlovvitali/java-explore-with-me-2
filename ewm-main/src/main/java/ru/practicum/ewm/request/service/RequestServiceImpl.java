package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.states.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.Status;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> findAllRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + eventId + " не найдено."));
        requestRepository.findByRequesterIdAndEventId(userId, eventId).ifPresent(
                request -> {
                    throw new ValidationException("Запрос уже существует");
                });
        if (event.getInitiator().equals(user)) {
            throw new ValidationException("Инициатор не может запрашивать участие.");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Невозможно участвовать в неопубликованном событии.");
        }
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventId(Status.CONFIRMED, eventId);
        if (!event.getParticipantLimit().equals(0L) && event.getParticipantLimit() == confirmedRequests.size()) {
            throw new ValidationException("Превышен лимит запросов.");
        }
        Status status;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = Status.CONFIRMED;
        } else {
            status = Status.PENDING;
        }
        Request request = Request.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(status)
                .build();
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id: " + requestId + " не найден"));
        request.setStatus(Status.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> findAllRequestsByEvent(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + eventId + " не найдено."));
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + eventId + " не найдено."));
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventId(Status.CONFIRMED, eventId);
        if (!event.getParticipantLimit().equals(0L) && event.getParticipantLimit() == confirmedRequests.size()) {
            throw new ValidationException("Превышен лимит запросов.");
        }
        List<Request> requests = requestRepository.findAllByIdIn(updateRequest.getRequestIds());
        List<Request> savedRequests = new ArrayList<>();
        if (!requests.isEmpty()) {
            if (requests.stream()
                    .map(Request::getStatus)
                    .anyMatch(status -> !status.equals(Status.PENDING))) {
                throw new ValidationException("Статус может быть изменен только для запросов в ожидании.");
            }
            requests.forEach(request -> request.setStatus(updateRequest.getStatus()));
            savedRequests = requestRepository.saveAll(requests);
        }
        List<ParticipationRequestDto> confirmedRequestsList = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequestsList = new ArrayList<>();

        for (Request request : savedRequests) {
            if (request.getStatus().equals(Status.CONFIRMED)) {
                ParticipationRequestDto requestDto = RequestMapper.toParticipationRequestDto(request);
                confirmedRequestsList.add(requestDto);
            } else if (request.getStatus().equals(Status.REJECTED)) {
                ParticipationRequestDto requestDto = RequestMapper.toParticipationRequestDto(request);
                rejectedRequestsList.add(requestDto);
            }
        }
        event.setConfirmedRequests((long) confirmedRequestsList.size());
        eventRepository.save(event);
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequestsList)
                .rejectedRequests(rejectedRequestsList)
                .build();
    }
}
