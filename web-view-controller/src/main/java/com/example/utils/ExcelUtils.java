package com.example.utils;

import com.example.entity.dto.PriceDTO;
import com.example.entity.dto.PriceListDTO;
import com.example.entity.dto.ProductDTO;
import com.example.enums.Unit;
import com.example.exceptions.BadRequestException;
import com.example.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class ExcelUtils {

    private final SubcategoryService subcategoryService;
    private final ProductService productService;
    private final PriceListService priceListService;
    private final PriceService priceService;
    private final StoreService storeService;

    @Autowired
    public ExcelUtils(SubcategoryService subcategoryService, ProductService productService, PriceListService priceListService, PriceService priceService, StoreService storeService) {
        this.subcategoryService = subcategoryService;
        this.productService = productService;
        this.priceListService = priceListService;
        this.priceService = priceService;
        this.storeService = storeService;
    }

    public Sheet readXlsxFile(MultipartFile multipartFile){
        log.info("Method readXlsxFile begin");
        String fileName = multipartFile.getOriginalFilename();

        if (fileName.startsWith(".xlsx", fileName.length() - 5)) {
            try (InputStream excelIs = multipartFile.getInputStream()) {
                Workbook wb = WorkbookFactory.create(excelIs);
                Sheet sheet = wb.getSheetAt(0);
                excelIs.close();
                return sheet;
            } catch(IOException e) {
                BadRequestException exception = new BadRequestException("Error! Failed to process: multipartFile could not be read");
                log.error(exception.getMessage());
                throw exception;
            }

        } else {
            BadRequestException exception = new BadRequestException("Error! The multipartFile should be a .xlsx");
            log.error(exception.getMessage());
            throw exception;
        }
    }

    public List<ProductDTO> createProducts(Sheet sheet){
        log.info("Method createProducts begin");
        int rows = sheet.getLastRowNum();
        List<ProductDTO> productDTOList = new ArrayList<>(rows);
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext())
        {
            Row currentRow = rowIterator.next();
            if (currentRow.getRowNum() == 0){
                currentRow = rowIterator.next();
            }
            ProductDTO productDTO = new ProductDTO();
            int columns =  currentRow.getLastCellNum();
            for(int j = 0; j < columns; j++){
                Cell cell = currentRow.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null)
                switch (j) {
                    case 0 -> {
                        Long subcategoryId =  Double.valueOf(cell.getNumericCellValue()).longValue();
                        try {
                            subcategoryService.idValidation(subcategoryId);
                        } catch (IllegalArgumentException ex){
                            IllegalArgumentException exception = new IllegalArgumentException(ex.getMessage() + ". Cell address " + cell.getAddress());
                            log.error(exception.getMessage());
                            throw exception;
                        }
                        productDTO.setSubcategoryId(subcategoryId);
                    }
                    case 1 -> {
                        String name =  cell.getStringCellValue();
                        productDTO.setName(name);
                    }
                    case 2 -> {
                        String brand =  cell.getStringCellValue();
                        productDTO.setBrand(brand);

                    }
                    case 3 -> {
                        int quantity = ((int) cell.getNumericCellValue());
                        productDTO.setQuantity(quantity);
                    }
                    case 4 -> {
                        String enumName =  cell.getStringCellValue();
                        Unit unit = Arrays.stream(Unit.values())
                                .filter(enumUnit -> enumUnit.toString().equals(enumName.toUpperCase()))
                                .findFirst().orElseThrow(() -> {
                                    IllegalArgumentException exception = new IllegalArgumentException("Error! Wrong unit of measure - "+ enumName +
                                            ", cell " + cell.getAddress() + ". Possible units of measure: MILLILITER, LITRE, GRAM, KILOGRAM, PIECE");
                                    log.error(exception.getMessage());
                                    return exception;
                                });
                        productDTO.setUnit(unit);
                    }
                    case 5 -> {
                        String manufacturer =  cell.getStringCellValue();
                        productDTO.setManufacturer(manufacturer);
                    }
                    default ->  {
                        IllegalArgumentException exception = new IllegalArgumentException("Error! Data not owned by Product in cell: " + cell.getAddress());
                        log.error(exception.getMessage());
                        throw exception;
                    }
                }
            }
            if (productDTO.getSubcategoryId() != null){
                productDTOList.add(productDTO);
            }
        }
        List<ProductDTO> resultList = new ArrayList<>();
        for (ProductDTO productDTO : productDTOList){
            resultList.add(productService.create(productDTO));
        }
        return resultList;
    }

    public List<PriceListDTO> createPriceLists(Sheet sheet){
        log.info("Method createPriceLists begin");
        int rows = sheet.getLastRowNum();
        List<PriceListDTO> priceListDTOS = new ArrayList<>(rows);
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext())
        {
            Row currentRow = rowIterator.next();
            if (currentRow.getRowNum() == 0) currentRow = rowIterator.next();
            PriceListDTO priceListDTO = new PriceListDTO();
            int columns =  currentRow.getLastCellNum();
            for(int j = 0; j < columns; j++){
                Cell cell = currentRow.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null)
                switch (j) {
                    case 0 -> {
                        Long storeId =  Double.valueOf(cell.getNumericCellValue()).longValue();
                        try {
                            storeService.idValidation(storeId);
                        } catch (IllegalArgumentException ex){
                            IllegalArgumentException exception = new IllegalArgumentException(ex.getMessage() + ". Cell address " + cell.getAddress());
                            log.error(exception.getMessage());
                            throw exception;
                        }
                        priceListDTO.setStoreId(storeId);
                    }
                    case 1 -> {
                        Long productId =  Double.valueOf(cell.getNumericCellValue()).longValue();
                        try {
                            productService.idValidation(productId);
                        } catch (IllegalArgumentException ex){
                            IllegalArgumentException exception = new IllegalArgumentException(ex.getMessage() + ". Cell address " + cell.getAddress());
                            log.error(exception.getMessage());
                            throw exception;
                        }
                        priceListDTO.setProductId(productId);
                    }
                    case 2 -> {
                        try {
                            BigDecimal currentPrice = BigDecimal.valueOf(cell.getNumericCellValue());
                            priceListDTO.setCurrentPrice(currentPrice);
                        } catch (NumberFormatException ex){
                            IllegalArgumentException exception = new IllegalArgumentException("Error! Invalid number format, cell address " + cell.getAddress());
                            log.error(exception.getMessage());
                            throw exception;
                        }
                    }
                    default -> {
                        IllegalArgumentException exception = new IllegalArgumentException("Error! Data not owned by PriceList in cell: " + cell.getAddress());
                        log.error(exception.getMessage());
                        throw exception;
                    }
                }
            }
            if (priceListDTO.getStoreId() != null){
                priceListDTOS.add(priceListDTO);
            }
        }
        List<PriceListDTO> resultList = new ArrayList<>();
        for (PriceListDTO priceListDTO : priceListDTOS){
            resultList.add(priceListService.create(priceListDTO));
        }
        return resultList;
    }

    public List<PriceDTO> createPrices(Sheet sheet){
        log.info("Method createPrices begin");
        int rows = sheet.getLastRowNum();
        List<PriceDTO> priceDTOS = new ArrayList<>(rows);
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext())
        {
            Row currentRow = rowIterator.next();
            if (currentRow.getRowNum() == 0) currentRow = rowIterator.next();

            PriceDTO priceDTO = new PriceDTO();
            int columns =  currentRow.getLastCellNum();
            for(int j = 0; j < columns; j++){
                Cell cell = currentRow.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null)
                    switch (j) {
                        case 0 -> {
                            Long priceListId = Double.valueOf(cell.getNumericCellValue()).longValue();
                            try {
                                priceListService.idValidation(priceListId);
                            } catch (IllegalArgumentException ex){
                                IllegalArgumentException exception = new IllegalArgumentException(ex.getMessage() + ". Cell address " + cell.getAddress());
                                log.error(exception.getMessage());
                                throw exception;
                            }
                            priceDTO.setPriceListId(priceListId);
                        }
                        case 1 -> {
                            try {
                                BigDecimal price = BigDecimal.valueOf(cell.getNumericCellValue());
                                priceDTO.setPrice(price);
                            } catch (NumberFormatException ex){
                                IllegalArgumentException exception = new IllegalArgumentException("Error! Invalid number format, cell address " + cell.getAddress());
                                log.error(exception.getMessage());
                                throw exception;
                            }
                        }
                        case 2 -> {
                            LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                            priceDTO.setDate(date);
                        }
                        default -> {
                            IllegalArgumentException exception = new IllegalArgumentException("Error! Data not owned by Price in cell: " + cell.getAddress());
                            log.error(exception.getMessage());
                            throw exception;
                        }
                    }
                }
            if (priceDTO.getPriceListId() != null){
                priceDTOS.add(priceDTO);
            }
        }
        List<PriceDTO> resultList = new ArrayList<>();
        for (PriceDTO priceDTO : priceDTOS){
            resultList.add(priceService.create(priceDTO));
        }
        return resultList;
    }
}
