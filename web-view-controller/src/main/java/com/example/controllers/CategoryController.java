package com.example.controllers;


import com.example.entity.dto.CategoryDTO;
import com.example.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path ="/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Category controller", description = "This controller is designed to get categories and edit them")
public class CategoryController {

        private final CategoryService categoryService;

        @Autowired
        public CategoryController(CategoryService categoryService) {
                this.categoryService = categoryService;
        }

        @Operation(
                summary = "Create a new category (only for admin role)",
                description = "Allows you to create only new categories"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping()
        @ResponseStatus(HttpStatus.CREATED)
        public CategoryDTO create(@RequestBody CategoryDTO categoryDTO) {
                return categoryService.create(categoryDTO);
        }

        @Operation(
                summary = "Get all category",
                description = "Allows you to get all categories"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping()
        public List<CategoryDTO> read(){
                return categoryService.listConverterToDTO(categoryService.findAll());
        }

        @Operation(
                summary = "Get category by id",
                description = "Allows you to get categories by id"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping("/{id}")
        public CategoryDTO read(@PathVariable("id") Long id) {
            return categoryService.convertToDTO(categoryService.findById(id));
        }

        @Operation(
                summary = "Update an existing category (only for admin role)",
                description = "Allows you to update an existing category"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping()
        public CategoryDTO update(@RequestBody CategoryDTO categoryDTO) {
            return categoryService.update(categoryDTO);
        }

        @Operation(
                summary = "Delete an existing category by id (only for admin role)",
                description = "Allows you to delete an existing category by id"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
            categoryService.deleteById(id);
        }

}
