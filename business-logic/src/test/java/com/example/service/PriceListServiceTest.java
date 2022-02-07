package com.example.service;

import com.example.entity.Price;
import com.example.entity.PriceList;
import com.example.entity.Product;
import com.example.entity.Store;
import com.example.entity.dto.PriceDTO;
import com.example.entity.dto.PriceListDTO;
import com.example.repository.IPriceListRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PriceListServiceTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private static IPriceListRepository priceListRepository;
    private static ProductService productService;
    private static StoreService storeService;
    private static PriceService priceService;
    private static PriceListService priceListService;

    private PriceList priceList;
    private PriceListDTO priceListDTO;
    private Product product;
    private Store store;
    private Price price1;
    private Price price2;
    private PriceDTO priceDTO1;
    private PriceDTO priceDTO2;

    private List<PriceList> priceLists;
    private List<Price> prices;

    @Captor
    ArgumentCaptor<PriceList> priceListCaptor;

    @BeforeAll
    static void setMock(){
        priceListRepository = Mockito.mock(IPriceListRepository.class);
        productService = Mockito.mock(ProductService.class);
        storeService = Mockito.mock(StoreService.class);
        priceService = Mockito.mock(PriceService.class);
        priceListService = new PriceListService(priceListRepository, productService, storeService);

        priceListService.setPriceService(priceService);
    }

    @BeforeEach
    void init(){
        priceList = new PriceList();
        priceList.setId(1L);
        priceList.setProduct(product);
        priceList.setCurrentPrice(new BigDecimal("99.99").setScale(2, RoundingMode.DOWN));

        product = new Product();
        product.setId(1L);
        product.setName("Молоко \"Бежин Луг\"");

        priceList.setProduct(product);

        store = new Store();
        store.setId(1L);
        store.setName("Глобус");

        priceList.setStore(store);

        priceLists = new ArrayList<>();
        priceLists.add(priceList);

        price1 = new Price(1L, new BigDecimal("65.12").setScale(2,
                RoundingMode.DOWN),
                LocalDate.of(2021,1,1),
                priceList);

        priceDTO1 = new PriceDTO(1L, new BigDecimal("65.12").setScale(2,
                RoundingMode.DOWN),
                LocalDate.of(2021,1,1),
                priceList.getId());

        price2 = new Price(2L, new BigDecimal("99.99").setScale(2,
                RoundingMode.DOWN),
                LocalDate.of(2021,2,2),
                priceList);

        priceDTO2 = new PriceDTO(2L, new BigDecimal("99.99").setScale(2,
                RoundingMode.DOWN),
                LocalDate.of(2021,2,2),
                priceList.getId());

        prices = new ArrayList<>();
        prices.add(price1);
        prices.add(price2);

        priceList.setPriceHistoryList(prices);

        priceListDTO = new PriceListDTO();
        priceListDTO.setId(priceList.getId());
        priceListDTO.setProductId(priceList.getProduct().getId());
        priceListDTO.setStoreId(priceList.getStore().getId());
        priceListDTO.setStoreName(priceList.getStore().getName());

        List<PriceDTO> priceDTOS = new ArrayList<>();
        priceDTOS.add(priceDTO1);
        priceDTOS.add(priceDTO2);

        priceListDTO.setPriceHistoryList(priceDTOS);
    }

    @AfterEach
    void reset(){
        Mockito.reset(priceListRepository);
        Mockito.reset(storeService);
        Mockito.reset(priceService);
        Mockito.reset(productService);
    }

    @Test
    public void convertToDTOShouldConvertPriceListEntityToPriceListDTO(){
        PriceListDTO priceListDTO = priceListService.convertToDTO(priceList);

        assertThat(priceList.getId(), equalTo(priceListDTO.getId()));
        assertThat(priceList.getProduct().getId(), equalTo(priceListDTO.getProductId()));
        assertThat(priceList.getStore().getId(), equalTo(priceListDTO.getStoreId()));
    }

    @Test
    public void convertFromDTOShouldConvertPriceListDTOEntityToPriceList(){
        doNothing().when(storeService).idValidation(1L);
        when(storeService.findById(1L)).thenReturn(store);
        doNothing().when(productService).idValidation(1L);
        when(productService.findById(1L)).thenReturn(product);
        when(priceListRepository.existsById(1L)).thenReturn(true);
        when(priceService.convertFromDTO(priceDTO1)).thenReturn(price1);
        when(priceService.convertFromDTO(priceDTO2)).thenReturn(price2);

        PriceList testPriceList = priceListService.convertFromDTO(priceListDTO);

        assertThat(priceList.getId(), equalTo(testPriceList.getId()));
        assertThat(priceList.getStore(), equalTo(testPriceList.getStore()));
        assertThat(priceList.getProduct(), equalTo(testPriceList.getProduct()));
        assertThat(priceList.getCurrentPrice(), equalTo(testPriceList.getCurrentPrice()));
        assertThat(priceList.getPriceHistoryList(), equalTo(testPriceList.getPriceHistoryList()));
    }

    @Test
    public void createShouldCreatePriceList(){
        priceListDTO.setId(null);
        doNothing().when(storeService).idValidation(1L);
        when(storeService.findById(1L)).thenReturn(store);
        doNothing().when(productService).idValidation(1L);
        when(productService.findById(1L)).thenReturn(product);
        when(priceListRepository.existsById(1L)).thenReturn(true);
        when(priceService.convertFromDTO(priceDTO1)).thenReturn(price1);
        when(priceService.convertFromDTO(priceDTO2)).thenReturn(price2);
        when(priceListRepository.save(any(PriceList.class))).thenReturn(priceList);

        priceListService.create(priceListDTO);

        verify(priceListRepository).save(priceListCaptor.capture());
        verify(priceListRepository, times(1)).save(any(PriceList.class));
        assertThat(priceListCaptor.getValue().getId(), equalTo(null));
        assertThat(priceListCaptor.getValue().getStore(), equalTo(priceList.getStore()));
        assertThat(priceListCaptor.getValue().getProduct(), equalTo(priceList.getProduct()));
        assertThat(priceListCaptor.getValue().getCurrentPrice(), equalTo(priceList.getCurrentPrice()));
        assertThat(priceListCaptor.getValue().getPriceHistoryList(), equalTo(priceList.getPriceHistoryList()));
    }

    @Test
    public void findAllShouldCallPriceListRepositoryMethodFindAll(){
        when(priceListRepository.findAll()).thenReturn(priceLists);

        priceListService.findAll();

        verify(priceListRepository, times(1)).findAll();
    }

    @Test
    public void findPriceListsForPriceComparisonByProductIdShouldCallPriceListRepositoryMethodFindAllPriceListsByProductsId(){
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(1L);
        when(priceListRepository.findAllPriceListsByProductsId(longs)).thenReturn(Optional.of(priceLists));


        priceListService.findPriceListsForPriceComparisonByProductId(longs);

        verify(priceListRepository, times(1)).findAllPriceListsByProductsId(longs);
    }

    @Test
    public void findPriceListsForComparisonOfPriceDynamicsByProductIdAndBetweenDateShouldFindAllPriceListsForComparisonOfPriceDynamicsByProductIdAndBetweenDate(){
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(1L);
        when(priceListRepository.findAllPriceListsByProductsId(longs)).thenReturn(Optional.of(priceLists));
        when(priceService.findAllPriceBetweenDateParam(1L,
                LocalDate.of(2021,1,1),
                LocalDate.of(2021,2,1))).thenReturn(prices);

        priceListService.findPriceListsForComparisonOfPriceDynamicsByProductIdAndBetweenDate(longs, LocalDate.of(2021,1,1), LocalDate.of(2021,2,1));

        verify(priceListRepository, times(1)).findAllPriceListsByProductsId(longs);
        verify(priceService, times(1)).findAllPriceBetweenDateParam(1L,
                LocalDate.of(2021,1,1),
                LocalDate.of(2021,2,1));
    }

    @Test
    public void findPriceListsForPriceComparisonByProductsIdAndStoresIdShouldFindAllPriceListsForPriceComparisonByProductsIdAndStoresId(){
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(1L);
        when(priceListRepository.findAllPriceListsByProductsIdAndStoresId(longs, longs)).thenReturn(Optional.of(priceLists));

        priceListService.findPriceListsForPriceComparisonByProductsIdAndStoresId(longs, longs);

        verify(priceListRepository, times(1)).findAllPriceListsByProductsIdAndStoresId(longs, longs);
    }

    @Test
    public void findPriceListsForComparisonOfPriceDynamicsByProductIdAndStoresIdAndBetweenDateShouldFindAllPriceListsForComparisonOfPriceDynamicsByProductIdAndStoresIdAndBetweenDate(){
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(1L);
        when(priceListRepository.findAllPriceListsByProductsIdAndStoresId(longs, longs)).thenReturn(Optional.of(priceLists));
        when(priceService.findAllPriceBetweenDateParam(1L,
                LocalDate.of(2021,1,1),
                LocalDate.of(2021,2,1))).thenReturn(prices);

        priceListService.findPriceListsForComparisonOfPriceDynamicsByProductIdAndStoresIdAndBetweenDate(longs, longs,
                LocalDate.of(2021,1,1),
                LocalDate.of(2021,2,1));

        verify(priceListRepository, times(1)).findAllPriceListsByProductsIdAndStoresId(longs, longs);
        verify(priceService, times(1)).findAllPriceBetweenDateParam(1L,
                LocalDate.of(2021,1,1),
                LocalDate.of(2021,2,1));
    }

    @Test
    public void findByIdShouldCallPriceListRepositoryMethodFindById(){
        when(priceListRepository.findById(1L)).thenReturn(Optional.of(priceList));

        priceListService.findById(1L);

        verify(priceListRepository, times(1)).findById(1L);
    }

    @Test
    public void updateShouldUpdatePriceList(){
        doNothing().when(storeService).idValidation(1L);
        when(storeService.findById(1L)).thenReturn(store);
        doNothing().when(productService).idValidation(1L);
        when(productService.findById(1L)).thenReturn(product);
        when(priceListRepository.existsById(1L)).thenReturn(true);
        when(priceService.convertFromDTO(priceDTO1)).thenReturn(price1);
        when(priceService.convertFromDTO(priceDTO2)).thenReturn(price2);

        when(priceListRepository.save(any(PriceList.class))).thenReturn(priceList);

        priceListService.update(priceListDTO);

        verify(priceListRepository, times(1)).save(priceListCaptor.capture());
        assertThat(priceListCaptor.getValue().getId(), equalTo(priceList.getId()));
        assertThat(priceListCaptor.getValue().getStore(), equalTo(priceList.getStore()));
        assertThat(priceListCaptor.getValue().getProduct(), equalTo(priceList.getProduct()));
        assertThat(priceListCaptor.getValue().getCurrentPrice(), equalTo(priceList.getCurrentPrice()));
        assertThat(priceListCaptor.getValue().getPriceHistoryList(), equalTo(priceList.getPriceHistoryList()));
    }

    @Test
    public void deleteByIdShouldCallPriceListRepositoryMethodDeleteById(){
        doNothing().when(priceListRepository).deleteById(1L);
        when(priceListRepository.existsById(1L)).thenReturn(true);

        priceListService.deleteById(1L);

        verify(priceListRepository, times(1)).deleteById(1L);
    }

    @Test
    public void findLastPriceShouldReturnLastPrice(){
        BigDecimal lastPrice = priceListService.findLastPrice(prices);

        assertThat(price2.getPrice(), equalTo(lastPrice));
    }

    @Test
    public void updateCurrentPriceByPriceListIdShouldUpdateCurrentPriceFieldInPriceListEntityAndSaveInRepository(){
        priceList.setCurrentPrice(new BigDecimal("1.00").setScale(2, RoundingMode.DOWN));
        when(priceListRepository.findById(1L)).thenReturn(Optional.of(priceList));
        when(priceListRepository.save(any(PriceList.class))).thenReturn(priceList);

        priceListService.updateCurrentPriceByPriceListId(1L);

        verify(priceListRepository, times(1)).findById(1L);
        verify(priceListRepository, times(1)).save(priceListCaptor.capture());
        assertThat(priceListCaptor.getValue().getCurrentPrice(), equalTo(new BigDecimal("99.99").setScale(2, RoundingMode.DOWN)));
    }

    @Test
    public void idValidationShouldThrowIllegalArgumentExceptionIfIdEqualsNullNotZero(){
        assertThatThrownBy(() -> priceListService.idValidation(null)).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> priceListService.idValidation(0L)).isInstanceOf(IllegalArgumentException.class);
    }
}
