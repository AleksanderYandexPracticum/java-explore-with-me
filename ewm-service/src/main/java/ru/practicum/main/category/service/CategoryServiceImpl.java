package ru.practicum.main.category.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.CategoryMapper;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.DuplicateNameException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.ValidationExceptionFindCategory;

import java.util.List;
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
        try {
            return CategoryMapper.toCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate of the categrory name");
            throw new DuplicateNameException("Duplicate of the categrory name");
        }
    }

    @Transactional
    @Override
    public CategoryDto updateCategoryAdmin(Long catId, NewCategoryDto newCategoryDto) {
        if (categoryRepository.findCategoryById(catId) == null) {
            throw new NotFoundException("The required object was not found.");
        }
        Category newCategory = CategoryMapper.toCategory(newCategoryDto);
        newCategory.setId(catId);
        CategoryDto categoryDto;
        try {
            categoryDto = CategoryMapper.toCategoryDto(categoryRepository.saveAndFlush(newCategory));
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate of the categrory name");
            throw new DuplicateNameException("Duplicate of the categrory name");
        }
        return categoryDto;
    }

    @Transactional
    @Override
    public void deleteCategoryAdmin(Long catId) {
        if (categoryRepository.getById(catId) == null) {
            throw new NotFoundException("The required object was not found.");
        }
        if (eventRepository.findFirstByCategoryId(catId) != null) {
            throw new ValidationExceptionFindCategory("The category is not empty");
        }
        categoryRepository.deleteCategoryById(catId);
    }


    @Transactional
    @Override
    public List<CategoryDto> getCategoryPublic(Integer from, Integer size) {

        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        return categoryRepository.findAll(pageable)
                .stream()
                .map((category) -> CategoryMapper.toCategoryDto(category))
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public CategoryDto getCategoryByIdPublic(Long catId) {
        Category category = categoryRepository.findCategoryById(catId);
        if (category == null) {
            throw new NotFoundException("The required object was not found.");
        }
        return CategoryMapper.toCategoryDto(category);
    }
}