package ru.practicum.main.category.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Fetch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.CategoryMapper;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.DuplicateNameException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.ValidationExceptionFindCategory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @Override
    public CategoryDto addCategoryAdmin(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toCategory(newCategoryDto);
        CategoryDto categoryDto;
        try {
            categoryDto = CategoryMapper.toCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate of the categrory name");
            throw new DuplicateNameException("Duplicate of the categrory name");
        }
        return categoryDto;
    }

    @Transactional
    @Override
    public CategoryDto updateCategoryAdmin(Long catId, NewCategoryDto newCategoryDto) {
        Category newCategory = CategoryMapper.toCategory(newCategoryDto);
        newCategory.setId(catId);
        return CategoryMapper.toCategoryDto(categoryRepository.save(newCategory));
    }

    @Transactional
    @Override
    public void deleteCategoryAdmin(Long catId) {

        Event event = eventRepository.findFirstByCategoryId(catId);
        if (event != null) {
            throw new ValidationExceptionFindCategory("The category is not empty");
        } else {
            Category deleteCategory = categoryRepository.removeCategoryById(catId);
            if (deleteCategory == null) {
                throw new NotFoundException("The required object was not found.");
            }
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getCategoryPublic(Integer from, Integer size) {

        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        return categoryRepository.findAll(pageable)
                .stream()
                .map((category) -> CategoryMapper.toCategoryDto(category))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    @Override
    public CategoryDto getCategoryByIdPublic(Long catId) {
        Optional<Category> category = Optional.ofNullable(categoryRepository.getById(catId));

        return CategoryMapper.toCategoryDto(category.orElseThrow(() -> new NotFoundException("The required object was not found.")));
    }
}