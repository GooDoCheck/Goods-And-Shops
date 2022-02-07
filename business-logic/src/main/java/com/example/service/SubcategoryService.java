package com.example.service;

import com.example.entity.Product;
import com.example.entity.Subcategory;
import com.example.entity.dto.SubcategoryDTO;
import com.example.exceptions.NotFoundException;
import com.example.repository.ISubcategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SubcategoryService implements EntityService<Subcategory, SubcategoryDTO> {
    private final ISubcategoryRepository subcategoryRepository;
    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public SubcategoryService(ISubcategoryRepository subcategoryRepository, ProductService productService, CategoryService categoryService) {
        this.subcategoryRepository = subcategoryRepository;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @PostConstruct
    public void init(){
        productService.setSubcategoryService(this);
        categoryService.setSubcategoryService(this);
    }

    @Override
    public SubcategoryDTO convertToDTO(Subcategory subcategory) {
        log.info("Method convertToDTO begin");
        SubcategoryDTO subcategoryDTO = new SubcategoryDTO();
        subcategoryDTO.setId(subcategory.getId());
        subcategoryDTO.setName(subcategory.getName());
        subcategoryDTO.setCategoryId(subcategory.getCategory().getId());
        ArrayList<Long> productsId = new ArrayList<>();
        if (subcategory.getProductList() != null) {
            for (Product product : subcategory.getProductList()) {
                productsId.add(product.getId());
            }
            subcategoryDTO.setProductsId(productsId);
        } else subcategoryDTO.setProductsId(null);
        return subcategoryDTO;
    }

    @Transactional
    @Override
    public Subcategory convertFromDTO(SubcategoryDTO subcategoryDTO) {
        log.info("Method convertFromDTO begin");
        Subcategory subcategory;
        if (subcategoryDTO.getId() != null && subcategoryDTO.getId() != 0L) {
            log.info("Condition where subcategory not new is met");
            idValidation(subcategoryDTO.getId());
            subcategory = subcategoryRepository.findById(subcategoryDTO.getId()).get();
            subcategory.setName(subcategoryDTO.getName());
            categoryService.idValidation(subcategoryDTO.getCategoryId());
            subcategory.setCategory(categoryService.findById(subcategoryDTO.getCategoryId()));

            subcategory.getProductList().removeAll(subcategory.getProductList());
            for (Long id : subcategoryDTO.getProductsId()){
                subcategory.getProductList().add(productService.findById(id));
            }

        } else {
            log.info("Condition where subcategory new is met");
            subcategory = new Subcategory();
            subcategory.setId(subcategoryDTO.getId());
            subcategory.setName(subcategoryDTO.getName());
            categoryService.idValidation(subcategoryDTO.getCategoryId());
            subcategory.setCategory(categoryService.findById(subcategoryDTO.getCategoryId()));
            subcategory.setProductList(null);
        }
        return subcategory;
    }

    @Override
    public List<SubcategoryDTO> listConverterToDTO(List<Subcategory> list) {
        log.info("Method listConverterToDTO begin");
        List<SubcategoryDTO> subcategoryDTOS = new ArrayList<>();
        for (Subcategory subcategory : list) {
            subcategoryDTOS.add(convertToDTO(subcategory));
        }
        return subcategoryDTOS;
    }

    @Transactional
    @Override
    public SubcategoryDTO create(SubcategoryDTO subcategoryDTO) {
        log.info("Method create begin");
        if (subcategoryDTO.getId() == null || subcategoryDTO.getId() != 0L) {
            Subcategory subcategory = convertFromDTO(subcategoryDTO);
            categoryService.idValidation(subcategoryDTO.getCategoryId());
            subcategory.setCategory(categoryService.findById(subcategoryDTO.getCategoryId()));
            subcategory.setProductList(null);
            Subcategory savedSubcategory = subcategoryRepository.save(subcategory);
            log.info("Subcategory " + savedSubcategory.getId() + " is created");
            return convertToDTO(savedSubcategory);
        } else {
            IllegalArgumentException exception = new IllegalArgumentException("Error! New subcategory id can only 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        }
    }

    @Transactional
    @Override
    public List<Subcategory> findAll() {
        log.info("Method findAll begin");
        List<Subcategory> subcategories =  subcategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        log.info("Method findAll result set size = " + subcategories.size());
        return subcategories;
    }

    @Transactional
    @Override
    public Subcategory findById(Long id) {
        log.info("Method findById begin");
        return subcategoryRepository.findById(id).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! Subcategory not found with id " + id);
            log.error(exception.getMessage());
            return exception;
        });
    }

    @Transactional
    @Override
    public SubcategoryDTO update(SubcategoryDTO subcategoryDTO) {
        log.info("Method update begin");
        idValidation(subcategoryDTO.getId());
        Subcategory subcategory = convertFromDTO(subcategoryDTO);
        Subcategory savedSubcategory = subcategoryRepository.save(subcategory);
        log.info("Subcategory " + savedSubcategory.getId() + " is updated");
        return convertToDTO(savedSubcategory);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        log.info("Method deleteById begin");
        idValidation(id);
        subcategoryRepository.deleteById(id);
        log.info("Subcategory " + id + " is deleted");
    }

    @Transactional
    @Override
    public void idValidation(Long id) {
        log.info("Method idValidation begin");
        if (id == null || id == 0L ) {
            IllegalArgumentException exception = new IllegalArgumentException("Error! Subcategory id cannot be 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        } else if (!subcategoryRepository.existsById(id)){
            IllegalArgumentException exception = new IllegalArgumentException("Error! Subcategory does not exist with this id - " + id);
            log.error(exception.getMessage());
            throw  exception;
        }
    }
}
