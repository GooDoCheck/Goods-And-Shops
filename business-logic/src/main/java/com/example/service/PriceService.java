package com.example.service;

import com.example.entity.Price;
import com.example.entity.dto.PriceDTO;
import com.example.exceptions.NotFoundException;
import com.example.repository.IPriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
@Service
public class PriceService implements EntityService<Price, PriceDTO> {
    private final IPriceRepository priceRepository;
    private final PriceListService priceListService;

    @Autowired
    public PriceService(IPriceRepository priceRepository, PriceListService priceListService) {
        this.priceRepository = priceRepository;
        this.priceListService = priceListService;
    }

    @PostConstruct
    public void init(){
        priceListService.setPriceService(this);
    }

    @Override
    public PriceDTO convertToDTO(Price price) {
        log.info("Method convertToDTO begin");
        PriceDTO priceDTO = new PriceDTO();
        priceDTO.setId(price.getId());
        priceDTO.setPrice(price.getPrice());
        priceDTO.setDate(price.getDate());
        priceDTO.setPriceListId(price.getPriceList().getId());
        return priceDTO;
    }

    @Override
    public Price convertFromDTO(PriceDTO priceDTO) {
        log.info("Method convertFromDTO begin");
        Price price = new Price();
        price.setId(priceDTO.getId());
        price.setPrice(priceDTO.getPrice());
        price.setDate(priceDTO.getDate());

        if (priceDTO.getPriceListId() == null){
            price.setPriceList(null);
        } else {
            priceListService.idValidation(priceDTO.getPriceListId());
            price.setPriceList(priceListService.findById(priceDTO.getPriceListId()));
        }

        return price;
    }

    @Override
    public List<PriceDTO> listConverterToDTO(List<Price> prices) {
        log.info("Method listConverterToDTO begin");
        List<PriceDTO> priceDTOS = new ArrayList<>();
        for (Price price : prices) {
            priceDTOS.add(convertToDTO(price));
        }
        return priceDTOS;
    }

    @Override
    public PriceDTO create(PriceDTO priceDTO) {
        log.info("Method create begin");
        if (priceDTO.getId() == null || priceDTO.getId() != 0L) {
            Price savedPrice = priceRepository.save(convertFromDTO(priceDTO));
            log.info("Price" + savedPrice.getId() + " is created");
            return convertToDTO(savedPrice);
        } else {
            IllegalArgumentException exception = new IllegalArgumentException("Error! New priceList id can only 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        }
    }

    @Override
    public List<Price> findAll() {
        log.info("Method findAll begin");
        List<Price> prices =  priceRepository.findAll();
        log.info("Method findAll result set size = " + prices.size());
        return prices;
    }

    @Transactional
    public List<Price> findAllPriceBetweenDateParam(Long priceListId, LocalDate startDate, LocalDate endDate) {
        log.info("Method findAllPriceBetweenDateParam begin");
        return priceRepository.findAllPriceBetweenDateParam(priceListId, startDate, endDate)
                .orElseThrow(()-> {
                    NotFoundException exception = new NotFoundException("Error! Price not found with priceListId " + priceListId + ", between startDate " + startDate.toString() + ", endDate " + endDate.toString());
                    log.error(exception.getMessage());
                    return exception;
                });
    }

    @Override
    public Price findById(Long id) {
        log.info("Method findById begin");
        return priceRepository.findById(id).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! Price not found with id " + id);
            log.error(exception.getMessage());
            return exception;
        });
    }

    @Override
    public PriceDTO update(PriceDTO priceDTO) {
        log.info("Method update begin");
        idValidation(priceDTO.getId());
        Price savedPrice = priceRepository.save(convertFromDTO(priceDTO));
        log.info("Price " + savedPrice.getId() + " is updated");
        return convertToDTO(savedPrice);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Method deleteById begin");
        idValidation(id);
        priceRepository.deleteById(id);
        log.info("Price " + id + " is deleted");
    }

    @Override
    public void idValidation(Long id) {
        log.info("Method idValidation begin");
        if (id == null || id == 0L ) {
            IllegalArgumentException exception = new IllegalArgumentException("Error! Price id cannot be 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        } else if (!priceRepository.existsById(id)){
            IllegalArgumentException exception = new IllegalArgumentException("Error! Price does not exist with this id - " + id);
            log.error(exception.getMessage());
            throw  exception;
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
