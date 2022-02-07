package com.example.service;

import com.example.entity.PriceList;
import com.example.entity.Product;
import com.example.entity.Subcategory;
import com.example.entity.dto.ProductDTO;
import com.example.repository.IProductRepository;
import com.example.repository.ISubcategoryRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private static IProductRepository productRepository;
    private static ISubcategoryRepository subcategoryRepository;
    private static SubcategoryService subcategoryService;
    private static PriceListService priceListService;
    private static ProductService productService;


    private Product product;
    private ProductDTO productDTO;
    private PriceList priceList;
    private List<PriceList> priceLists;
    private List<Product> productList;

    private Subcategory subcategory;


    @Captor
    ArgumentCaptor<Product> productCaptor;

    @BeforeAll
    static void setMock(){
        productRepository = Mockito.mock(IProductRepository.class);
        subcategoryRepository = Mockito.mock(ISubcategoryRepository.class);
        subcategoryService = Mockito.mock(SubcategoryService.class);
        priceListService = Mockito.mock(PriceListService.class);
        productService = new ProductService(productRepository,subcategoryRepository);
        productService.setSubcategoryService(subcategoryService);
        productService.setPriceListService(priceListService);
    }

    @BeforeEach
    void init(){
        product = new Product();
        product.setId(1L);
        product.setName("Молоко \"Бежин Луг\"");

        subcategory = new Subcategory();
        subcategory.setId(1L);
        product.setSubcategory(subcategory);

        priceList = new PriceList();
        priceList.setId(1L);
        priceList.setProduct(product);
        priceLists = new ArrayList<>();
        priceLists.add(priceList);

        product.setPriceList(priceLists);

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setName(product.getName());
        productDTO.setSubcategoryId(product.getSubcategory().getId());

        ArrayList<Long> priceListsIds = new ArrayList<>();
        priceListsIds.add(priceLists.get(0).getId());

        productDTO.setPriceListsId(priceListsIds);

        productList = new ArrayList<>();
        productList.add(product);

    }

    @AfterEach
    void reset(){
        Mockito.reset(productRepository);
        Mockito.reset(subcategoryRepository);
        Mockito.reset(subcategoryService);
        Mockito.reset(priceListService);
    }

    @Test
    public void convertToDTOShouldConvertProductEntityToProductDTO(){
        ProductDTO productDTO = productService.convertToDTO(product);

        assertThat(product.getId(), equalTo(productDTO.getId()));
        assertThat(product.getName(), equalTo(productDTO.getName()));
        assertThat(product.getSubcategory().getId(), equalTo(productDTO.getSubcategoryId()));
        assertThat(product.getPriceList().get(0).getId(), equalTo(productDTO.getPriceListsId().get(0)));
    }

    @Test
    public void convertFromDTOShouldConvertProductDTOEntityToProduct(){
        doNothing().when(subcategoryService).idValidation(1L);
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory));

        Product testProduct = productService.convertFromDTO(productDTO);

        verify(subcategoryRepository, times(1)).findById(1L);
        assertThat(productDTO.getId(), equalTo(testProduct.getId()));
        assertThat(productDTO.getName(), equalTo(testProduct.getName()));
        assertThat(productDTO.getSubcategoryId(), equalTo(testProduct.getSubcategory().getId()));
        assertThat(null, equalTo(testProduct.getPriceList()));
    }

    @Test
    public void createShouldCreateProduct(){
        productDTO.setId(null);
        doNothing().when(subcategoryService).idValidation(1L);
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory));

        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.create(productDTO);

        verify(subcategoryRepository).findById(1L);
        verify(productRepository).save(productCaptor.capture());
        verify(productRepository, times(1)).save(any(Product.class));
        assertThat(productCaptor.getValue().getId(), equalTo(productDTO.getId()));
        assertThat(productCaptor.getValue().getName(), equalTo(productDTO.getName()));
        assertThat(productCaptor.getValue().getSubcategory().getId(), equalTo(productDTO.getSubcategoryId()));
        assertThat(productCaptor.getValue().getPriceList(), is(nullValue()));
    }

    @Test
    public void findAllShouldCallProductRepositoryMethodFindAll(){
        when(productRepository.findAll(any(Sort.class))).thenReturn(productList);

        productService.findAll();

        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void findProductsByKeywordShouldCallProductRepositoryMethodFindAllProductByKeywordContains(){
        when(productRepository.findAllProductByKeywordContains(any(String.class), any(Sort.class))).thenReturn(Optional.of(productList));

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        productService.findProductsByKeyword("keyword", sort);

        verify(productRepository, times(1)).findAllProductByKeywordContains("keyword", sort);
    }

    @Test
    public void findProductsByCategoryAndByKeywordShouldCallProductRepositoryMethodFindAllProductByKeywordContainsAndByCategoryName(){
        when(productRepository.findAllProductByKeywordContainsAndByCategoryName(any(String.class), any(String.class), any(Sort.class))).thenReturn(Optional.of(productList));

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        productService.findProductsByCategoryAndByKeyword("categoryName","keyword", sort);

        verify(productRepository, times(1)).findAllProductByKeywordContainsAndByCategoryName("categoryName","keyword", sort);
    }

    @Test
    public void findProductsBySubcategoryAndByKeywordShouldCallProductRepositoryMethodFindAllProductByKeywordContainsAndBySubcategoryName(){
        when(productRepository.findAllProductByKeywordContainsAndBySubcategoryName(any(String.class), any(String.class), any(Sort.class))).thenReturn(Optional.of(productList));

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        productService.findProductsBySubcategoryAndByKeyword("subcategoryName","keyword", sort);

        verify(productRepository, times(1)).findAllProductByKeywordContainsAndBySubcategoryName("subcategoryName","keyword", sort);
    }

    @Test
    public void findByIdShouldCallProductRepositoryMethodFindById(){
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.findById(1L);

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void updateShouldUpdateProduct(){
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(priceListService.findById(1L)).thenReturn(priceList);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        doNothing().when(subcategoryService).idValidation(1L);
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory));

        productService.update(productDTO);

        verify(productRepository, times(1)).save(productCaptor.capture());
        verify(productRepository, times(1   )).save(any(Product.class));
        assertThat(productCaptor.getValue().getId(), equalTo(productDTO.getId()));
        assertThat(productCaptor.getValue().getName(), equalTo(productDTO.getName()));
        assertThat(productCaptor.getValue().getSubcategory().getId(), equalTo(productDTO.getSubcategoryId()));
        assertThat(productCaptor.getValue().getPriceList(), equalTo(priceLists));
    }

    @Test
    public void deleteByIdShouldCallProductRepositoryMethodDeleteById(){
        doNothing().when(productRepository).deleteById(1L);
        when(productRepository.existsById(any(Long.class))).thenReturn(true);

        productService.deleteById(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    public void idValidationShouldThrowIllegalArgumentExceptionIfIdEqualsNullNotZero(){
        assertThatThrownBy(() -> {
            productService.idValidation(null);;
        }).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> {
            productService.idValidation(0L);;
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
