package com.example.service;

import com.example.entity.Category;
import com.example.entity.Subcategory;
import com.example.entity.dto.CategoryDTO;
import com.example.entity.dto.SubcategoryDTO;
import com.example.repository.ICategoryRepository;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private static ICategoryRepository categoryRepository;
    private static SubcategoryService subcategoryService;
    private static CategoryService categoryService;


    private Category category;
    private CategoryDTO categoryDTO;
    private Subcategory subcategory;
    private SubcategoryDTO subcategoryDTO;
    private List<Category> categoryList;

    @Captor
    ArgumentCaptor<Category> categoryCaptor;

    @BeforeAll
    static void setMock(){
        subcategoryService = Mockito.mock(SubcategoryService.class);
        categoryRepository = Mockito.mock(ICategoryRepository.class);
        categoryService = new CategoryService(categoryRepository);
        categoryService.setSubcategoryService(subcategoryService);
    }

    @BeforeEach
    void init(){
        category = new Category();
        category.setId(1L);
        category.setName("Бакалея");

        subcategory = new Subcategory();
        subcategory.setCategory(category);
        List<Subcategory> subcategories = new ArrayList<>();
        subcategories.add(subcategory);
        category.setSubcategoryList(subcategories);

        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("Бакалея");

        subcategoryDTO = new SubcategoryDTO();
        subcategoryDTO.setCategoryId(category.getId());
        List<SubcategoryDTO> subcategoryDTOS = new ArrayList<>();
        subcategoryDTOS.add(subcategoryDTO);
        categoryDTO.setSubcategoryList(subcategoryDTOS);

        categoryList = new ArrayList<Category>();
        categoryList.add(category);

    }

    @AfterEach
    void reset(){
        Mockito.reset(categoryRepository);
        Mockito.reset(subcategoryService);
    }


    @Test
    public void convertToDTOShouldConvertCategoryEntityToCategoryDTO(){
        when(subcategoryService.convertToDTO(any())).thenReturn(subcategoryDTO);

        CategoryDTO testCategoryDTO = categoryService.convertToDTO(category);

        verify(subcategoryService, times(1)).convertToDTO(subcategory);
        assertThat(categoryDTO.getId(), equalTo(testCategoryDTO.getId()));
        assertThat(categoryDTO.getName(), equalTo(testCategoryDTO.getName()));
    }

    @Test
    public void convertFromDTOShouldConvertCategoryDTOEntityToCategory(){
        when(subcategoryService.convertFromDTO(any(SubcategoryDTO.class))).thenReturn(subcategory);

        Category testCategory = categoryService.convertFromDTO(categoryDTO);

        verify(subcategoryService, times(1)).convertFromDTO(subcategoryDTO);
        assertThat(categoryDTO.getId(), equalTo(testCategory.getId()));
        assertThat(categoryDTO.getName(), equalTo(testCategory.getName()));
    }

    @Test
    public void createShouldCreateCategory(){
        categoryDTO.setId(null);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(subcategoryService.convertFromDTO(any(SubcategoryDTO.class))).thenReturn(subcategory);

        categoryService.create(this.categoryDTO);

        verify(categoryRepository).save(categoryCaptor.capture());
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertThat(categoryCaptor.getValue().getId(), equalTo(categoryDTO.getId()));
        assertThat(categoryCaptor.getValue().getName(), equalTo(categoryDTO.getName()));
    }

    @Test
    public void findAllShouldCallCategoryRepositoryMethodFindAll(){
        when(categoryRepository.findAll(any(Sort.class))).thenReturn(categoryList);

        categoryService.findAll();

        verify(categoryRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    public void findByIdShouldCallCategoryRepositoryMethodFindById(){
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.findById(1L);

        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    public void updateShouldUpdateCategory(){
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(subcategoryService.convertFromDTO(any(SubcategoryDTO.class))).thenReturn(subcategory);
        when(categoryRepository.existsById(any(Long.class))).thenReturn(true);
        categoryService.update(this.categoryDTO);

        verify(categoryRepository).save(categoryCaptor.capture());
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertThat(categoryCaptor.getValue().getId(), equalTo(categoryDTO.getId()));
        assertThat(categoryCaptor.getValue().getName(), equalTo(categoryDTO.getName()));
    }

    @Test
    public void deleteByIdShouldCallCategoryRepositoryMethodDeleteById(){
        doNothing().when(categoryRepository).deleteById(any(Long.class));
        when(categoryRepository.existsById(any(Long.class))).thenReturn(true);
        categoryService.deleteById(1L);

        verify(categoryRepository, times(1)).deleteById(any(Long.class));
    }

    @Test
    public void idValidationShouldThrowIllegalArgumentExceptionIfIdEqualsNullNotZero(){


        assertThatThrownBy(() -> {
            categoryService.idValidation(null);;
        }).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> {
            categoryService.idValidation(0L);;
        }).isInstanceOf(IllegalArgumentException.class);
    }

}
