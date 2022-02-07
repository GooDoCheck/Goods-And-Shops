package com.example.controllers;


import com.example.entity.dto.ProductDTO;
import com.example.exceptions.BadRequestException;
import com.example.service.ProductService;
import com.example.utils.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Product controller", description = "This controller is designed to get products and edit them")
@Slf4j
@RestController
@RequestMapping(path ="/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

        @Autowired
        private ProductService productService;
        @Autowired
        private ExcelUtils excelUtils;

        @Operation(
                summary = "Create a new product (only for admin role)",
                description = "Allows you to create only new product"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping()
        @ResponseStatus(HttpStatus.CREATED)
        public ProductDTO create(@RequestBody ProductDTO productDTO) {
                return productService.create(productDTO);
        }

        @Operation(
                summary = "Import from excel file (only for admin role)",
                description = "Allows import products from excel file"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @ResponseStatus(HttpStatus.OK)
        public List<ProductDTO> create(@Parameter(description = "Excel file named importProducts") @RequestParam("importProducts") MultipartFile multipartFile) {
               Sheet sheet = excelUtils.readXlsxFile(multipartFile);
               return excelUtils.createProducts(sheet);
        }

        @Operation(
                summary = "Get all products with params",
                description = "Allows you to get all products with params"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping()
        public List<ProductDTO> findProducts(@Parameter(description = "Category name, do not use at the same time as the subcategory name (optional)") @RequestParam(required = false) String categoryName,
                                             @Parameter(description = "Subcategory name, do not use at the same time as the category name (optional)") @RequestParam(required = false) String subcategoryName,
                                             @Parameter(description = "Keyword to search for a product, by name, manufacturer or brand (optional)") @RequestParam(name = "search_keyword",required = false) String searchKeyword,
                                             @Parameter(description = "Sorting direction - asc or desc (required)") @RequestParam(name = "sorting_direction") String sortingDirection){
                log.info("Endpoint findProducts begin");
                Sort.Direction direction;
                try {
                        if (sortingDirection == null || sortingDirection.isEmpty()) sortingDirection = "asc";
                        direction = Sort.Direction.fromString(sortingDirection);
                } catch (IllegalArgumentException ex) {
                        BadRequestException exception = new BadRequestException("Error! Invalid parameter sorting_direction: " + sortingDirection + ". Valid parameter: ASC, DESC.");
                        log.error(exception.getMessage());
                        throw exception;
                }
                Sort sort = Sort.by(direction, "name");

                if (categoryName != null & subcategoryName != null) {
                        BadRequestException exception = new BadRequestException("Error! Invalid request: sorting by categoryName and subcategoryName is not allowed at the same time!");
                        log.error(exception.getMessage());
                        throw exception;
                } else if (categoryName != null && !categoryName.isEmpty()) {
                        log.info("Condition where categoryName not null and not empty is met");
                        return productService.listConverterToDTO(
                                productService.findProductsByCategoryAndByKeyword(categoryName, searchKeyword, sort));
                } else if (subcategoryName != null && !subcategoryName.isEmpty()) {
                        log.info("Condition where subcategoryName not null and not empty is met");
                        return productService.listConverterToDTO(
                                productService.findProductsBySubcategoryAndByKeyword(subcategoryName, searchKeyword, sort));
                } else {
                        log.info("Condition where categoryName and subcategoryName null or empty is met");
                        return productService.listConverterToDTO(
                                productService.findProductsByKeyword(searchKeyword, sort));
                }
        }

        @Operation(
                summary = "Get product by id",
                description = "Allows you to get product by id"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping("/{id}")
        public ProductDTO read(@PathVariable("id") Long id){
            return productService.convertToDTO(productService.findById(id));
        }

        @Operation(
                summary = "Update an existing product (only for admin role)",
                description = "Allows you to update an existing product"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping()
        public ProductDTO update(@RequestBody ProductDTO productDTO){
            return productService.update(productDTO);
        }

        @Operation(
                summary = "Delete an existing product by id (only for admin role)",
                description = "Allows you to delete an existing product by id"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
            productService.deleteById(id);
        }

}
