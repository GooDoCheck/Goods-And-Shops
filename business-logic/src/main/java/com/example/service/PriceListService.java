package com.example.service;

import com.example.entity.Price;
import com.example.entity.PriceList;
import com.example.entity.dto.PriceDTO;
import com.example.entity.dto.PriceListDTO;
import com.example.exceptions.NotFoundException;
import com.example.repository.IPriceListRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PriceListService implements EntityService<PriceList, PriceListDTO> {
    private final IPriceListRepository priceListRepository;
    private final ProductService productService;
    private final StoreService storeService;
    @Getter
    @Setter
    private PriceService priceService;

    @Autowired
    public PriceListService(IPriceListRepository priceListRepository, ProductService productService, StoreService storeService) {
        this.priceListRepository = priceListRepository;
        this.productService = productService;
        this.storeService = storeService;
    }

    @PostConstruct
    public void init(){
        productService.setPriceListService(this);
    }

    @Override
    public PriceListDTO convertToDTO(PriceList priceList) {
        log.info("Method convertToDTO begin");
        PriceListDTO priceListDTO = new PriceListDTO();
        priceListDTO.setId(priceList.getId());
        priceListDTO.setStoreId(priceList.getStore().getId());
        priceListDTO.setStoreName(priceList.getStore().getName());
        priceListDTO.setProductId(priceList.getProduct().getId());
        priceListDTO.setCurrentPrice(priceList.getCurrentPrice());

        List<PriceDTO> priceDTOS = new ArrayList<>();
        for (Price price : priceList.getPriceHistoryList()){
            priceDTOS.add(priceService.convertToDTO(price));
        }
        priceListDTO.setPriceHistoryList(priceDTOS);
        return priceListDTO;
    }

    @Override
    public PriceList convertFromDTO(PriceListDTO priceListDTO) {
        log.info("Method convertFromDTO begin");
        PriceList priceList = new PriceList();
        priceList.setId(priceListDTO.getId());

        storeService.idValidation(priceListDTO.getStoreId());
        priceList.setStore(storeService.findById(priceListDTO.getStoreId()));

        productService.idValidation((priceListDTO.getProductId()));
        priceList.setProduct(productService.findById(priceListDTO.getProductId()));

        List<Price> prices = new ArrayList<>();
        if (priceListDTO.getPriceHistoryList() != null && !priceListDTO.getPriceHistoryList().isEmpty()){
            for (PriceDTO priceDTO : priceListDTO.getPriceHistoryList()){
                Price price = priceService.convertFromDTO(priceDTO);
                price.setPriceList(priceList);
                prices.add(price);
            }
            priceList.setCurrentPrice(findLastPrice(prices));
        } else {
            priceList.setCurrentPrice(priceListDTO.getCurrentPrice().setScale(2, RoundingMode.DOWN));
        }

        priceList.setPriceHistoryList(prices);
        return priceList;
    }

    @Override
    public List<PriceListDTO> listConverterToDTO(List<PriceList> priceLists) {
        log.info("Method listConverterToDTO begin");
        List<PriceListDTO> priceListDTOS = new ArrayList<>();
        for (PriceList priceList : priceLists) {
            priceListDTOS.add(convertToDTO(priceList));
        }
        return priceListDTOS;
    }

    @Transactional
    @Override
    public PriceListDTO create(PriceListDTO priceListDTO) {
        log.info("Method create begin");
        if (priceListDTO.getId() == null || priceListDTO.getId() != 0L) {
            PriceList savedPriceList = priceListRepository.save(convertFromDTO(priceListDTO));
            log.info("PriceList" + savedPriceList.getId() + " is created");
            return convertToDTO(savedPriceList);
        } else {
            IllegalArgumentException exception = new IllegalArgumentException("Error! New priceList id can only 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        }
    }

    @Transactional
    @Override
    public List<PriceList> findAll() {
        log.info("Method findAll begin");
        List<PriceList> priceLists = priceListRepository.findAll();
        log.info("Method findAll result set size = " + priceLists.size());
        return  priceLists;
    }

    @Transactional
    public List<PriceList> findPriceListsForPriceComparisonByProductId(ArrayList<Long> productsId) {
        log.info("Method findPriceListsForPriceComparisonByProductId begin");
        return priceListRepository.findAllPriceListsByProductsId(productsId)
                .orElseThrow(() -> {
                    NotFoundException exception = new NotFoundException("Error! PriceLists not found with productsId " + productsId.toString() );
                    log.error(exception.getMessage());
                    return exception;
                });
    }

    @Transactional
    public List<PriceList> findPriceListsForComparisonOfPriceDynamicsByProductIdAndBetweenDate(ArrayList<Long> productsId,
                                                                                               LocalDate startDate,
                                                                                               LocalDate endDate) {
        log.info("Method findPriceListsForComparisonOfPriceDynamicsByProductIdAndBetweenDate begin");
        log.info("Stage 1: get priceLists with productsId " + productsId.toString());
        List<PriceList> tempPriceLists = priceListRepository.findAllPriceListsByProductsId(productsId).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! PriceLists not found with productsId " + productsId.toString());
            log.error(exception.getMessage());
            return exception;
        });
        log.info("Stage 2: get prices for each priceList between startDate " + startDate.toString() + ", endDate " + endDate.toString() + ", and set in priceHistoryList");
        for (PriceList priceList : tempPriceLists){
            List<Price> prices = priceService.findAllPriceBetweenDateParam(priceList.getId(), startDate, endDate);
            priceList.setPriceHistoryList(prices);
        }
        return tempPriceLists;
    }

    @Transactional
    public List<PriceList> findPriceListsForPriceComparisonByProductsIdAndStoresId(ArrayList<Long> productsId,
                                                                                   ArrayList<Long> storesId) {
        log.info("Method findPriceListsForPriceComparisonByProductsIdAndStoresId begin");
        return priceListRepository.findAllPriceListsByProductsIdAndStoresId(productsId, storesId)
                .orElseThrow(() -> {
                    NotFoundException exception = new NotFoundException("Error! PriceLists not found with productsId " + productsId.toString() + ", and storesId " + storesId.toString());
                    log.error(exception.getMessage());
                    return exception;
                });
    }

    @Transactional
    public List<PriceList> findPriceListsForComparisonOfPriceDynamicsByProductIdAndStoresIdAndBetweenDate(ArrayList<Long> productsId,
                                                                                                          ArrayList<Long> storesId,
                                                                                                          LocalDate startDate,
                                                                                                          LocalDate endDate) {
        log.info("Method findPriceListsForComparisonOfPriceDynamicsByProductIdAndStoresIdAndBetweenDate begin");
        log.info("Stage 1: get priceLists with productsId " + productsId.toString() + ", and storesId " + storesId.toString());
        List<PriceList> tempPriceLists = priceListRepository.findAllPriceListsByProductsIdAndStoresId(productsId, storesId).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! PriceLists not found with productsId " + productsId.toString() + ", and storesId " + storesId.toString());
            log.error(exception.getMessage());
            return exception;
        });
        for (PriceList priceList : tempPriceLists){
            List<Price> prices = priceService.findAllPriceBetweenDateParam(priceList.getId(), startDate, endDate);
            priceList.setPriceHistoryList(prices);
        }
        return tempPriceLists;
    }

    @Transactional
    @Override
    public PriceList findById(Long id) {
        log.info("Method findById begin");
        return priceListRepository.findById(id).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! PriceList not found with id " + id);
            log.error(exception.getMessage());
            return exception;
        }) ;
    }

    @Transactional
    @Override
    public PriceListDTO update(PriceListDTO priceListDTO) {
        log.info("Method findById begin");
        idValidation(priceListDTO.getId());
        PriceList savedPriceList = priceListRepository.save(convertFromDTO(priceListDTO));
        log.info("PriceList" + savedPriceList.getId() + " is updated");
        return convertToDTO(savedPriceList);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        log.info("Method deleteById begin");
        idValidation(id);
        priceListRepository.deleteById(id);
        log.info("PriceList " + id + " is deleted");
    }

    public BigDecimal findLastPrice(List<Price> prices){
        log.info("Method findLastPrice begin");
        LocalDate lastDate = prices.get(0).getDate();
        BigDecimal lastPrice = prices.get(0).getPrice().setScale(2, RoundingMode.DOWN);
        for (Price price : prices){
            if (price.getDate().compareTo(lastDate) > 0){
                lastDate = price.getDate();
                lastPrice = price.getPrice().setScale(2, RoundingMode.DOWN);
            }
        }
        return lastPrice;
    }

    @Transactional
    public void updateCurrentPriceByPriceListId(Long priceListId) {
        log.info("Method updateCurrentPriceByPriceListId begin");
        PriceList priceList = findById(priceListId);
        priceList.setCurrentPrice(findLastPrice(priceList.getPriceHistoryList()));
        priceListRepository.save(priceList);
        log.info("PriceList " + priceList.getId() + ", current price is updated");
    }

    @Transactional
    @Override
    public void idValidation(Long id) {
        log.info("Method idValidation begin");
        if (id == null || id == 0L ) {
            IllegalArgumentException exception = new IllegalArgumentException("Error! PriceList id cannot be 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        } else if (!priceListRepository.existsById(id)){
            IllegalArgumentException exception = new IllegalArgumentException("Error! PriceList does not exist with this id - " + id);
            log.error(exception.getMessage());
            throw  exception;
        }
    }
}
