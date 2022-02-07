package com.example.controllers;


import com.example.entity.dto.PriceDTO;
import com.example.service.PriceListService;
import com.example.service.PriceService;
import com.example.utils.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.service.PriceService.distinctByKey;

@Tag(name = "Price controller", description = "This controller is designed to get prices and edit them")
@RestController
@RequestMapping(path ="/prices")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PriceController {

        private final PriceService priceService;
        private final PriceListService priceListService;
        private final ExcelUtils excelUtils;

        @Autowired
        public PriceController(PriceService priceService, PriceListService priceListService, ExcelUtils excelUtils) {
                this.priceService = priceService;
                this.priceListService = priceListService;
                this.excelUtils = excelUtils;
        }

        @Operation(
                summary = "Create a new price (only for admin role)",
                description = "Allows you to create only new price"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping()
        @ResponseStatus(HttpStatus.CREATED)
        public PriceDTO create(@RequestBody PriceDTO priceDTO) {
                PriceDTO savedPriceDTO = priceService.create(priceDTO);
                priceListService.updateCurrentPriceByPriceListId(savedPriceDTO.getPriceListId());
            return savedPriceDTO;
        }

        @Operation(
                summary = "Import from excel file(only for admin role)",
                description = "Allows import prices from excel file"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @ResponseStatus(HttpStatus.OK)
        public List<PriceDTO> create(@Parameter(description = "Excel file named importPrices") @RequestParam("importPrices") MultipartFile multipartFile) {
                Sheet sheet = excelUtils.readXlsxFile(multipartFile);
                List<PriceDTO> prices = excelUtils.createPrices(sheet),
                        pricesFiltered = prices.stream()
                        .filter(distinctByKey(PriceDTO::getPriceListId))
                        .collect(Collectors.toList());
                for (PriceDTO priceDTO : pricesFiltered){
                        priceListService.updateCurrentPriceByPriceListId(priceDTO.getPriceListId());
                }
                return prices;
        }

        @Operation(
                summary = "Get all price",
                description = "Allows you to get all prices"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping()
        public List<PriceDTO> read(){
                return priceService.listConverterToDTO(priceService.findAll());
        }

        @Operation(
                summary = "Get price by id",
                description = "Allows you to get price by id"
        )
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @GetMapping("/{id}")
        public PriceDTO read(@PathVariable("id") Long id){
            return priceService.convertToDTO(priceService.findById(id));
        }

        @Operation(
                summary = "Update an existing price (only for admin role)",
                description = "Allows you to update an existing price"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping()
        public PriceDTO update(@RequestBody PriceDTO priceDTO){
                PriceDTO tempPriceDTO = priceService.update(priceDTO);
                priceListService.updateCurrentPriceByPriceListId(tempPriceDTO.getPriceListId());
            return tempPriceDTO;

        }

        @Operation(
                summary = "Delete an existing price by id (only for admin role)",
                description = "Allows you to delete an existing price by id"
        )
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
                Long priceListId = priceService.findById(id).getPriceList().getId();
                priceService.deleteById(id);
                priceListService.updateCurrentPriceByPriceListId(priceListId);
        }
}
