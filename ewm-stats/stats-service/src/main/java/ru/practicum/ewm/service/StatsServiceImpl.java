package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.mapper.endpoint.EndpointHitMapper;
import ru.practicum.ewm.mapper.stats.ViewStatsMapper;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            return ViewStatsMapper.toDtoList(statsRepository.findUniqueViewStats(start, end, uris));
        } else {
            return ViewStatsMapper.toDtoList(statsRepository.findViewStats(start, end, uris));
        }
    }

    @Transactional
    @Override
    public EndpointHitDto save(EndpointHitDto dto) {
        return EndpointHitMapper.toDto(statsRepository.save(EndpointHitMapper.toEntity(dto)));
    }
}
