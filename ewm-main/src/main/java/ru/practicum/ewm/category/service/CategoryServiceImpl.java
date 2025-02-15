package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Создание новой категории: {}", newCategoryDto.getName());
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            log.warn("Категория с названием '{}' уже существует", newCategoryDto.getName());
            throw new ValidationException("Категория с таким названием уже существует.");
        }
        Category category = categoryRepository.save(CategoryMapper.newCategoryToCategory(newCategoryDto));
        log.info("Категория '{}' успешно создана с ID {}", category.getName(), category.getId());
        return CategoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public void deleteCategory(long catId) {
        log.info("Удаление категории с ID {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID " + catId + " не найдена."));
        if (eventRepository.existsByCategoryId(catId)) {
            log.warn("Невозможно удалить категорию с ID {}, так как существуют связанные события", catId);
            throw new ValidationException("Нельзя удалить категорию, к которой привязаны события.");
        }
        categoryRepository.deleteById(catId);
        log.info("Категория с ID {} успешно удалена", catId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(long catId, CategoryDto categoryDto) {
        log.info("Обновление категории с ID {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID " + catId + " не найдена."));
        Category categoryByName = categoryRepository.findByName(categoryDto.getName());
        if (categoryByName != null && catId != categoryByName.getId()) {
            log.warn("Категория с названием '{}' уже существует", categoryDto.getName());
            throw new ValidationException("Категория с таким названием уже существует.");
        }
        categoryDto.setId(catId);
        Category categorySaved = categoryRepository.save(CategoryMapper.dtoToCategory(categoryDto));
        log.info("Категория с ID {} успешно обновлена", catId);
        return CategoryMapper.toCategoryDto(categorySaved);
    }

    @Override
    public List<CategoryDto> findAllCategories(int from, int size) {
        log.info("Получение списка всех категорий, с {} до {}", from, from + size);
        PageRequest pageable = PageRequest.of(from, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        log.info("Найдено {} категорий", categories.size());
        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findCategory(long catId) {
        log.info("Поиск категории с ID {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID " + catId + " не найдена."));
        log.info("Категория с ID {} найдена: {}", catId, category.getName());
        return CategoryMapper.toCategoryDto(category);
    }
}
