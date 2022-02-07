package com.example.service;

import com.example.entity.Price;
import com.example.entity.PriceList;
import com.example.entity.dto.PriceDTO;
import com.example.repository.IPriceRepository;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
public class PriceServiceTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private IPriceRepository priceRepository;
    @Mock
    private PriceListService priceListService;
    @InjectMocks
    private PriceService priceService;

    private Price price;
    private PriceDTO priceDTO;
    private PriceList priceList;

    private List<Price> prices;

    @Captor
    ArgumentCaptor<Price> priceCaptor;

    @BeforeEach
    void init(){
        priceList = new PriceList();
        priceList.setId(1L);
        priceList.setCurrentPrice(new BigDecimal("99.99").setScale(2, RoundingMode.DOWN));
        price = new Price(1L, new BigDecimal("65.12").setScale(2,
                RoundingMode.DOWN),
                LocalDate.of(2021,1,1),
                priceList);

        priceDTO = new PriceDTO(1L, new BigDecimal("65.12").setScale(2,
                RoundingMode.DOWN),
                LocalDate.of(2021,1,1),
                priceList.getId());

        prices = new ArrayList<>();
        prices.add(price);

        priceList.setPriceHistoryList(prices);
    }

    @AfterEach
    void reset(){
        Mockito.reset(priceRepository);
        Mockito.reset(priceListService);
    }

    @Test
    public void convertToDTOShouldConvertPriceEntityToPriceDTO(){
        PriceDTO testPriceDTO = priceService.convertToDTO(price);

        assertThat(testPriceDTO.getId(), equalTo(price.getId()));
        assertThat(testPriceDTO.getPrice(), equalTo(price.getPrice()));
        assertThat(testPriceDTO.getDate(), equalTo(price.getDate()));
        assertThat(testPriceDTO.getPriceListId(), equalTo(price.getPriceList().getId()));
    }

    @Test
    public void convertFromDTOShouldConvertPriceDTOEntityToPrice(){
        doNothing().when(priceListService).idValidation(1L);
        when(priceListService.findById(1L)).thenReturn(priceList);

        Price testPrice = priceService.convertFromDTO(priceDTO);

        assertThat(testPrice.getId(), equalTo(priceDTO.getId()));
        assertThat(testPrice.getPrice(), equalTo(priceDTO.getPrice()));
        assertThat(testPrice.getDate(), equalTo(priceDTO.getDate()));
        assertThat(testPrice.getPriceList().getId(), equalTo(priceDTO.getPriceListId()));
    }

    @Test
    public void createShouldCreatePrice(){
        priceDTO.setId(null);
        doNothing().when(priceListService).idValidation(1L);
        when(priceListService.findById(1L)).thenReturn(priceList);
        when(priceRepository.save(any(Price.class))).thenReturn(price);

        priceService.create(priceDTO);

        verify(priceRepository, times(1)).save(priceCaptor.capture());
        assertThat(priceCaptor.getValue().getId(), equalTo(null));
        assertThat(priceCaptor.getValue().getPrice(), equalTo(price.getPrice()));
        assertThat(priceCaptor.getValue().getDate(), equalTo(price.getDate()));
        assertThat(priceCaptor.getValue().getPriceList(), equalTo(price.getPriceList()));
    }

    @Test
    public void findAllShouldCallPriceRepositoryMethodFindAll(){
        when(priceRepository.findAll()).thenReturn(prices);

        priceService.findAll();

        verify(priceRepository, times(1)).findAll();
    }

    @Test
    public void findAllPriceBetweenDateParamShouldCallPriceRepositoryMethodFindAllPriceBetweenDateParam(){
        when(priceRepository.findAllPriceBetweenDateParam(1L, LocalDate.of(2021,1,1),
                LocalDate.of(2021,2,1))).thenReturn(Optional.of(prices));

        priceService.findAllPriceBetweenDateParam(1L, LocalDate.of(2021,1,1),
                LocalDate.of(2021,2,1));

        verify(priceRepository, times(1)).findAllPriceBetweenDateParam(1L, LocalDate.of(2021,1,1),
                LocalDate.of(2021,2,1));
    }

    @Test
    public void findByIdShouldCallPriceRepositoryMethodFindById(){
        when(priceRepository.findById(1L)).thenReturn(Optional.of(price));

        priceService.findById(1L);

        verify(priceRepository, times(1)).findById(1L);
    }

    @Test
    public void updateShouldUpdatePrice(){
        when(priceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(priceListService).idValidation(1L);
        when(priceListService.findById(1L)).thenReturn(priceList);
        when(priceRepository.save(any(Price.class))).thenReturn(price);

        priceService.update(priceDTO);

        verify(priceRepository, times(1)).save(priceCaptor.capture());
        assertThat(priceCaptor.getValue().getId(), equalTo(price.getId()));
        assertThat(priceCaptor.getValue().getPrice(), equalTo(price.getPrice()));
        assertThat(priceCaptor.getValue().getDate(), equalTo(price.getDate()));
        assertThat(priceCaptor.getValue().getPriceList(), equalTo(price.getPriceList()));
    }

    @Test
    public void deleteByIdShouldCallPriceRepositoryMethodDeleteById(){
        doNothing().when(priceRepository).deleteById(1L);
        when(priceRepository.existsById(1L)).thenReturn(true);

        priceService.deleteById(1L);

        verify(priceRepository, times(1)).deleteById(1L);
    }

    @Test
    public void idValidationShouldThrowIllegalArgumentExceptionIfIdEqualsNullNotZero(){
        assertThatThrownBy(() -> priceService.idValidation(null)).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> priceService.idValidation(0L)).isInstanceOf(IllegalArgumentException.class);
    }

}
