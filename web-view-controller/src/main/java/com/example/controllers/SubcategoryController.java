package com.example.controllers;


import com.sun.jdi.InvalidTypeException;
import com.example.entity.dto.SubcategoryDTO;
import com.example.service.SubcategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Subcategory controller", description = "This controller is designed to get subcategories and edit them")
@RestController
@RequestMapping(path ="/subcategories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SubcategoryController {

        private final SubcategoryService subcategoryService;

        @Autowired
        public SubcategoryController(SubcategoryService subcategoryService) {
                this.subcategoryService = subcategoryService;
        }

        @Operation(
                summary = "Create a new subcategory (only for admin role)",
                description = "Allows you to create only new subcategory"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping()
        @ResponseStatus(HttpStatus.CREATED)
        public SubcategoryDTO create(@RequestBody SubcategoryDTO subcategoryDTO) {
            return subcategoryService.create(subcategoryDTO);
        }

        @Operation(
                summary = "Get all subcategories",
                description = "Allows you to get all subcategories"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping()
        public List<SubcategoryDTO> read(){
                return subcategoryService.listConverterToDTO(subcategoryService.findAll());
        }

        @Operation(
                summary = "Get subcategory by id",
                description = "Allows you to get subcategory by id"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping("/{id}")
        public  SubcategoryDTO read(@PathVariable("id") Long id){
            return subcategoryService.convertToDTO(subcategoryService.findById(id));
        }

        @Operation(
                summary = "Update an existing subcategory (only for admin role)",
                description = "Allows you to update an existing subcategory"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping()
        public SubcategoryDTO update(@RequestBody SubcategoryDTO subcategoryDTO) throws InvalidTypeException {
               return subcategoryService.update(subcategoryDTO);
        }

        @Operation(
                summary = "Delete an existing subcategory by id (only for admin role)",
                description = "Allows you to delete an existing subcategory by id"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
            subcategoryService.deleteById(id);
        }

}
