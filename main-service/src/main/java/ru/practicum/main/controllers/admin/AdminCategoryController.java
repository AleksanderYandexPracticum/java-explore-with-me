package ru.practicum.main.controllers.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.service.CategoryServiceImpl;
import ru.practicum.main.category.service.CategoryService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@RestController
@RequestMapping(path = "/admin/categories")
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Autowired
    public AdminCategoryController(CategoryServiceImpl adminCategoryServiceImpl) {
        this.categoryService = adminCategoryServiceImpl;
    }

    @PostMapping
    public CategoryDto addCategoryAdmin(HttpServletRequest request,
                                        @Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());

        return categoryService.addCategoryAdmin(newCategoryDto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategoryAdmin(HttpServletRequest request,
                                           @Positive @PathVariable Long catId,
                                           @Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        return categoryService.updateCategoryAdmin(catId, newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    public void deleteCategoryAdmin(HttpServletRequest request,
                                    @Positive @PathVariable("catId") Long catId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        categoryService.deleteCategoryAdmin(catId);
    }
}