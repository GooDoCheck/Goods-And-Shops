package com.example.service;

import com.example.entity.Category;
import com.example.entity.Subcategory;
import com.example.entity.dto.CategoryDTO;
import com.example.entity.dto.SubcategoryDTO;
import com.example.exceptions.NotFoundException;
import com.example.repository.ICategoryRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CategoryService implements EntityService<Category, CategoryDTO> {

    private final ICategoryRepository categoryRepository;
    @Getter
    @Setter
    private SubcategoryService subcategoryService;

    @Autowired
    public CategoryService(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDTO convertToDTO(Category category) {
        log.info("Method convertToDTO begin");
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        List<SubcategoryDTO> subcategoryDTOS = new ArrayList<>();
        for (Subcategory subcategory : category.getSubcategoryList()){
            subcategoryDTOS.add(subcategoryService.convertToDTO(subcategory));
        }
        categoryDTO.setSubcategoryList(subcategoryDTOS);
        return categoryDTO;
    }

    @Transactional
    @Override
    public Category convertFromDTO(CategoryDTO categoryDTO) {
        log.info("Method convertFromDTO begin");
        Category category = new Category();
        category.setId(categoryDTO.getId());
        category.setName(categoryDTO.getName());
        List<Subcategory> subcategories = new ArrayList<>();

        for (SubcategoryDTO subcategoryDTO : categoryDTO.getSubcategoryList()){
            Subcategory subcategory = subcategoryService.convertFromDTO(subcategoryDTO);
            if (subcategory.getCategory() == null) {
                subcategory.setCategory(category);
            }
            subcategories.add(subcategory);
        }
        category.setSubcategoryList(subcategories);
        return category;
    }

    @Override
    public List<CategoryDTO> listConverterToDTO(List<Category> categories) {
        log.info("Method listConverterToDTO begin");
        List<CategoryDTO> categoryDTOS = new ArrayList<>();
        for (Category category : categories) {
            categoryDTOS.add(convertToDTO(category));
        }
        return categoryDTOS;
    }

    @Transactional
    @Override
    public CategoryDTO create(CategoryDTO categoryDTO) {
        log.info("Method create begin");
        if (categoryDTO.getId() == null || categoryDTO.getId() != 0L) {
            Category category = categoryRepository.save(convertFromDTO(categoryDTO));
            log.info("Category " + category.getId() + " is created");
            return convertToDTO(category);
        } else {
            IllegalArgumentException exception = new IllegalArgumentException("Error! New category id can only 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        }
    }

    @Transactional
    @Override
    public List<Category> findAll() {
        log.info("Method findAll begin");
        List<Category> categoryList = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        log.info("Method findAll result set size = " + categoryList.size());
        return categoryList;
    }

    @Transactional
    @Override
    public Category findById(Long id) {
        log.info("Method findById begins");
        return categoryRepository.findById(id).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! Catalog not found with id " + id);
            log.error(exception.getMessage());
            return exception;
        });
    }

    @Transactional
    @Override
    public CategoryDTO update(CategoryDTO categoryDTO) {
        log.info("Method update begin");
        idValidation(categoryDTO.getId());
        Category category = categoryRepository.save(convertFromDTO(categoryDTO));
        log.info("Category " + category.getId() + " is updated");
        return convertToDTO(category);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        log.info("Method deleteById begin");
        idValidation(id);
        categoryRepository.deleteById(id);
        log.info("Category" + id + " is deleted");
    }

    @Transactional
    @Override
    public void idValidation(Long id) {
        log.info("Method idValidation begin");
        if (id == null || id == 0L ) {
            IllegalArgumentException exception = new IllegalArgumentException("Error! Category id cannot be 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        } else if (!categoryRepository.existsById(id)){
            IllegalArgumentException exception = new IllegalArgumentException("Error! Category does not exist with this id - " + id);
            log.error(exception.getMessage());
            throw  exception;
        }
    }
}
