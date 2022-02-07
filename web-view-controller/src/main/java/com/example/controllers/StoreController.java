package com.example.controllers;


import com.example.entity.Store;
import com.example.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Store controller", description = "This controller is designed to get stores and edit them")
@RestController
@RequestMapping(path ="/stores")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StoreController {

        private final StoreService storeService;

        @Autowired
        public StoreController(StoreService storeService) {
                this.storeService = storeService;
        }

        @Operation(
                summary = "Create a new store (only for admin role)",
                description = "Allows you to create only new store"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping()
        @ResponseStatus(HttpStatus.CREATED)
        public Store create(@RequestBody Store store) {
            return storeService.create(store);
        }

        @Operation(
                summary = "Get all stores with params",
                description = "Allows you to get all stores with params"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping()
        public List<Store> readAll(@Parameter(description = "Store name (optional)") @RequestParam(name = "store_name",required = false) String storeName,
                                   @Parameter(description = "City name (optional)") @RequestParam(name = "city_name", required = false) String cityName,
                                   @Parameter(description = "Sorting direction - asc or desc (optional)") @RequestParam(name = "sorting_direction", required = false) String sortingDirection){
                if (storeName != null | cityName != null){
                        return storeService.getStoresByCityOrNameEquals(cityName, storeName, sortingDirection);
                } else return storeService.findAll();
        }

        @Operation(
                summary = "Get store by id",
                description = "Allows you to get store by id"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping("/{id}")
        public Store read(@PathVariable("id") Long id){
            return storeService.findById(id);
        }

        @Operation(
                summary = "Update an existing store (only for admin role)",
                description = "Allows you to update an existing store"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping()
        public Store update(@RequestBody Store store){
                return storeService.update(store);
        }

        @Operation(
                summary = "Delete an existing store by id (only for admin role)",
                description = "Allows you to delete an existing store by id"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
            storeService.deleteById(id);
        }

}
