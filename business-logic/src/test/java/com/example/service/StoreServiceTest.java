package com.example.service;

import com.example.entity.Store;
import com.example.repository.IStoreRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private IStoreRepository storeRepository;
    @InjectMocks
    private StoreService storeService;

    private Store store;
    private List<Store> stores;

    @Captor
    ArgumentCaptor<Store> storeCaptor;

    @BeforeEach
    void init(){
        store = new Store();
        store.setId(1L);
        store.setName("Глобус");
        store.setCity("Тула");

        stores = new ArrayList<>();
        stores.add(store);
    }

    @AfterEach
    void reset(){
        Mockito.reset(storeRepository);
    }


    @Test
    public void createShouldCreateStore(){
        store.setId(null);
        when(storeRepository.save(any(Store.class))).thenReturn(store);

        storeService.create(store);

        verify(storeRepository, times(1)).save(storeCaptor.capture());
        assertThat(storeCaptor.getValue().getId(), equalTo(null));
        assertThat(storeCaptor.getValue().getName(), equalTo(store.getName()));
    }

    @Test
    public void findAllShouldCallStoreRepositoryMethodFindAll(){
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        when(storeRepository.findAll(sort)).thenReturn(stores);

        storeService.findAll();

        verify(storeRepository, times(1)).findAll(sort);
    }

    @Test
    public void getStoresByCityOrNameEqualsShouldCallPriceRepositoryMethodGetStoresByCityAndNameEquals(){
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        when(storeRepository.getStoresByCityAndNameEquals(sort, "Тула", "Глобус")).thenReturn(Optional.of(stores));

        storeService.getStoresByCityOrNameEquals("Тула", "Глобус", "asc");

        verify(storeRepository, times(1)).getStoresByCityAndNameEquals(sort,"Тула", "Глобус");
    }

    @Test
    public void getStoresByCityOrNameEqualsShouldCallPriceRepositoryMethodGetStoresByCityEquals(){
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        when(storeRepository.getStoresByCityEquals(sort, "Тула")).thenReturn(Optional.of(stores));

        storeService.getStoresByCityOrNameEquals("Тула", null, "asc");

        verify(storeRepository, times(1)).getStoresByCityEquals(sort,"Тула");
    }

    @Test
    public void getStoresByCityOrNameEqualsShouldCallPriceRepositoryMethodGetStoresStoresByNameEquals(){
        Sort sort = Sort.by(Sort.Direction.ASC, "city");
        when(storeRepository.getStoresByNameEquals(sort, "Глобус")).thenReturn(Optional.of(stores));

        storeService.getStoresByCityOrNameEquals(null, "Глобус", "asc");

        verify(storeRepository, times(1)).getStoresByNameEquals(sort,"Глобус");
    }

    @Test
    public void findByIdShouldCallStoreRepositoryMethodFindById(){
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        storeService.findById(1L);

        verify(storeRepository, times(1)).findById(1L);
    }

    @Test
    public void updateShouldUpdateStore(){
        when(storeRepository.existsById(1L)).thenReturn(true);
        when(storeRepository.save(any(Store.class))).thenReturn(store);

        storeService.update(store);

        verify(storeRepository, times(1)).save(storeCaptor.capture());
        assertThat(storeCaptor.getValue().getId(), equalTo(store.getId()));
        assertThat(storeCaptor.getValue().getName(), equalTo(store.getName()));
        assertThat(storeCaptor.getValue().getCity(), equalTo(store.getCity()));
    }

    @Test
    public void deleteByIdShouldCallStoreRepositoryMethodDeleteById(){
        doNothing().when(storeRepository).deleteById(1L);
        when(storeRepository.existsById(1L)).thenReturn(true);

        storeService.deleteById(1L);

        verify(storeRepository, times(1)).deleteById(1L);
    }

    @Test
    public void idValidationShouldThrowIllegalArgumentExceptionIfIdEqualsNullNotZero(){
        assertThatThrownBy(() -> storeService.idValidation(null)).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> storeService.idValidation(0L)).isInstanceOf(IllegalArgumentException.class);
    }

}
