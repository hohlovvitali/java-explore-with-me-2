package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations;
        if (Objects.nonNull(pinned)) {
            compilations = compilationRepository.findByPinned(pinned, pageRequest);
        } else {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        }
        if (compilations.isEmpty()) {
            return new ArrayList<>();
        }
        return compilations.stream()
                .map(compilation -> CompilationMapper.toCompilationDto(compilation, compilation.getEvents().stream()
                        .map(EventMapper::toEventShortDto)
                        .toList()))
                .toList();
    }

    @Override
    public CompilationDto findById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        List<EventShortDto> events = compilation.getEvents().stream()
                .map(EventMapper::toEventShortDto)
                .toList();
        return CompilationMapper.toCompilationDto(compilation, events);
    }

    @Transactional
    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        List<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
            compilation.setEvents(events);
        }
        Compilation createdCompilation = compilationRepository.save(compilation);
        List<EventShortDto> eventDtos = events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();
        return CompilationMapper.toCompilationDto(createdCompilation, eventDtos);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateDto) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        List<Event> events = new ArrayList<>();
        if (updateDto.getEvents() != null) {
            events = eventRepository.findAllByIdIn(updateDto.getEvents());
            compilation.setEvents(events);
        }
        if (updateDto.getPinned() != null) compilation.setPinned(updateDto.getPinned());

        if (updateDto.getTitle() != null) compilation.setTitle(updateDto.getTitle());
        Compilation savedCompilation = compilationRepository.save(compilation);
        List<EventShortDto> eventDtos = events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();
        return CompilationMapper.toCompilationDto(savedCompilation, eventDtos);
    }
}
