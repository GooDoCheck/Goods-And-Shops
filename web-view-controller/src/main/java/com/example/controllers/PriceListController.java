package com.example.controllers;


import com.example.entity.PriceList;
import com.example.entity.dto.PriceListDTO;
import com.example.service.PriceListService;
import com.example.utils.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "PriceList controller", description = "This controller is designed to get priceLists and edit them")
@Slf4j
@RestController
@RequestMapping(path ="/price_lists")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PriceListController {

        private final PriceListService priceListService;
        private final ExcelUtils excelUtils;

        @Autowired
        public PriceListController(PriceListService priceListService, ExcelUtils excelUtils) {
                this.priceListService = priceListService;
                this.excelUtils = excelUtils;
        }

        @Operation(
                summary = "Create a new priceList (only for admin role)",
                description = "Allows you to create only new priceList"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping()
        @ResponseStatus(HttpStatus.CREATED)
        public PriceListDTO create(@RequestBody PriceListDTO priceListDTO) {
            return priceListService.create(priceListDTO);
        }

        @Operation(
                summary = "Import from excel file (only for admin role)",
                description = "Allows import priceLists from excel file"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @ResponseStatus(HttpStatus.OK)
        public List<PriceListDTO> create(@Parameter(description = "Excel file named importPriceLists") @RequestParam("importPriceLists") MultipartFile multipartFile) {
                Sheet sheet = excelUtils.readXlsxFile(multipartFile);
                return excelUtils.createPriceLists(sheet);
        }

        @Operation(
                summary = "Get all priceLists with params",
                description = "Allows you to get all priceLists with params"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping()
        public List<PriceListDTO> findPriceLists(@Parameter(description = "Product id (optional)") @RequestParam(name = "productsId", required = false) ArrayList<Long> productsId,
                                                 @Parameter(description = "Store id (optional)") @RequestParam(name = "stores_id", required = false) ArrayList<Long> storesId,
                                                 @Parameter(description = "Start date, mandatory with end date parameter (optional)") @RequestParam(name = "start_date", required = false) LocalDate startDate,
                                                 @Parameter(description = "End date, mandatory with start date parameter (optional)") @RequestParam(name = "end_date", required = false) LocalDate endDate){
                log.info("Endpoint findPriceLists begin");
                List<PriceList> resultPriceLists;
                if (productsId != null || productsId.isEmpty()) {
                        log.info("Condition where productsId not null or not empty is met");
                        if (storesId == null || storesId.isEmpty()) {
                                log.info("Condition where storeId = null or empty is met");
                                if (startDate == null & endDate == null) {
                                        log.info("Condition where startDate and endDater null is met");
                                        resultPriceLists = priceListService.findPriceListsForPriceComparisonByProductId(productsId);
                                } else {
                                        resultPriceLists = priceListService.findPriceListsForComparisonOfPriceDynamicsByProductIdAndBetweenDate(productsId, startDate, endDate);
                                }
                        } else {
                                log.info("Condition where storeId not null or not empty is met");
                                if (startDate == null & endDate == null) {
                                        resultPriceLists = priceListService.findPriceListsForPriceComparisonByProductsIdAndStoresId(productsId, storesId);
                                } else {
                                        log.info("Condition where startDate and endDater not null is met");
                                        resultPriceLists = priceListService.findPriceListsForComparisonOfPriceDynamicsByProductIdAndStoresIdAndBetweenDate(productsId, storesId, startDate, endDate);
                                }
                        }
                } else {
                        log.info("Condition where productsId null or empty is met");
                        resultPriceLists = priceListService.findAll();
                }
                return priceListService.listConverterToDTO(resultPriceLists);
        }


        @Operation(
                summary = "Get priceList by id",
                description = "Allows you to get priceList by id"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping("/{id}")
        public PriceListDTO read(@PathVariable("id") Long id) {
            return priceListService.convertToDTO(priceListService.findById(id));
        }


        @Operation(
                summary = "Update an existing priceList (only for admin role)",
                description = "Allows you to update an existing priceList"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping()
        public PriceListDTO update(@RequestBody PriceListDTO priceListDTO) {
            return priceListService.update(priceListDTO);

        }

        @Operation(
                summary = "Delete an existing priceList by id (only for admin role)",
                description = "Allows you to delete an existing priceList by id"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
            priceListService.deleteById(id);
        }

}
