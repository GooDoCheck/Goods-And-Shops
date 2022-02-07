package com.example.service;

import com.example.entity.Category;
import com.example.entity.Product;
import com.example.entity.Subcategory;
import com.example.entity.dto.CategoryDTO;
import com.example.entity.dto.SubcategoryDTO;
import com.example.repository.ISubcategoryRepository;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
public class SubcategoryServiceTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private ISubcategoryRepository subcategoryRepository;
    @Mock
    private ProductService productService;
    @Mock
    private CategoryService categoryService;
    @InjectMocks
    private SubcategoryService subcategoryService;


    private Category category;
    private CategoryDTO categoryDTO;
    private Subcategory subcategory;
    private SubcategoryDTO subcategoryDTO;
    private Product product;
    private List<Subcategory> subcategoryList;
    private List<Product> productList;

    @Captor
    ArgumentCaptor<Subcategory> subcategoryCaptor;

    @BeforeEach
    void init(){
        category = new Category();
        category.setId(1L);
        category.setName("Бакалея");

        subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Крупы");
        subcategory.setCategory(category);
        subcategory.setProductList(new ArrayList<>());

        product = new Product();
        product.setId(1L);

        productList = new ArrayList<>();
        productList.add(product);
        subcategory.setProductList(productList);

        subcategoryList = new ArrayList<>();
        subcategoryList.add(subcategory);
        category.setSubcategoryList(subcategoryList);

        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("Бакалея");

        subcategoryDTO = new SubcategoryDTO();
        subcategoryDTO.setId(1L);
        subcategoryDTO.setName("Крупы");
        subcategoryDTO.setCategoryId(category.getId());

        List<Long> productsId = new ArrayList<>();
        productsId.add(1L);
        subcategoryDTO.setProductsId(productsId);

        List<SubcategoryDTO> subcategoryDTOS = new ArrayList<>();
        subcategoryDTOS.add(subcategoryDTO);
        categoryDTO.setSubcategoryList(subcategoryDTOS);
    }

    @AfterEach
    void reset(){
        Mockito.reset(subcategoryRepository);
        Mockito.reset(productService);
        Mockito.reset(categoryService);
    }

    @Test
    public void convertToDTOShouldConvertSubcategoryEntityToSubcategoryDTO(){

        SubcategoryDTO testSubcategoryDTO = subcategoryService.convertToDTO(subcategory);

        assertThat(subcategory.getId(), equalTo(testSubcategoryDTO.getId()));
        assertThat(subcategory.getName(), equalTo(testSubcategoryDTO.getName()));
        assertThat(subcategory.getCategory().getId(), equalTo(testSubcategoryDTO.getCategoryId()));
    }


    @Test
    public void convertFromDTOShouldConvertSubcategoryDTOEntityToSubcategory(){
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory));
        when(subcategoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryService).idValidation(1L);
        when(categoryService.findById(1L)).thenReturn(category);
        when(productService.findById(1L)).thenReturn(product);

        Subcategory testSubcategory = subcategoryService.convertFromDTO(subcategoryDTO);

        verify(subcategoryRepository, times(1)).findById(1L);
        verify(categoryService, times(1)).idValidation(1L);
        verify(categoryService, times(1)).findById(1L);
        verify(productService, times(1)).findById(1L);
        assertThat(subcategoryDTO.getId(), equalTo(testSubcategory.getId()));
        assertThat(subcategoryDTO.getName(), equalTo(testSubcategory.getName()));
    }

    @Test
    public void createShouldCreateSubcategory(){
        subcategoryDTO.setId(null);
        doNothing().when(categoryService).idValidation(1L);
        when(categoryService.findById(1L)).thenReturn(category);
        when(subcategoryRepository.save(any(Subcategory.class))).thenReturn(subcategory);

        subcategoryService.create(subcategoryDTO);

        verify(subcategoryRepository).save(subcategoryCaptor.capture());
        verify(subcategoryRepository, times(1)).save(any(Subcategory.class));
        assertThat(subcategoryCaptor.getValue().getId(), equalTo(subcategoryDTO.getId()));
        assertThat(subcategoryCaptor.getValue().getName(), equalTo(subcategoryDTO.getName()));
    }

    @Test
    public void findAllShouldCallCategoryRepositoryMethodFindAll(){
        when(subcategoryRepository.findAll(any(Sort.class))).thenReturn(subcategoryList);

        subcategoryService.findAll();

        verify(subcategoryRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    public void findByIdShouldCallCategoryRepositoryMethodFindById(){
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory));

        subcategoryService.findById(1L);

        verify(subcategoryRepository, times(1)).findById(1L);
    }

    @Test
    public void updateShouldUpdateSubcategory(){
        when(subcategoryRepository.existsById(1L)).thenReturn(true);

        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory));
        doNothing().when(categoryService).idValidation(1L);
        when(categoryService.findById(1L)).thenReturn(category);
        when(productService.findById(1L)).thenReturn(product);

        when(subcategoryRepository.save(any(Subcategory.class))).thenReturn(subcategory);

        subcategoryService.update(subcategoryDTO);

        verify(subcategoryRepository).save(subcategoryCaptor.capture());
        verify(subcategoryRepository, times(1)).save(any(Subcategory.class));
        assertThat(subcategoryCaptor.getValue().getId(), equalTo(subcategoryDTO.getId()));
        assertThat(subcategoryCaptor.getValue().getName(), equalTo(subcategoryDTO.getName()));

    }

    @Test
    public void deleteByIdShouldCallSubcategoryRepositoryMethodDeleteById(){
        doNothing().when(subcategoryRepository).deleteById(any(Long.class));
        when(subcategoryRepository.existsById(1L)).thenReturn(true);
        subcategoryService.deleteById(1L);

        verify(subcategoryRepository, times(1)).deleteById(1L);
    }

    @Test
    public void idValidationShouldThrowIllegalArgumentExceptionIfIdEqualsNullNotZero(){

        assertThatThrownBy(() -> subcategoryService.idValidation(null)).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> subcategoryService.idValidation(0L)).isInstanceOf(IllegalArgumentException.class);
    }

}
