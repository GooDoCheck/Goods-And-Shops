package com.example.service;

import com.example.entity.Store;
import com.example.exceptions.BadRequestException;
import com.example.exceptions.NotFoundException;
import com.example.repository.IStoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class StoreService {

    private final IStoreRepository storeRepository;

    @Autowired
    public StoreService(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Transactional
    public Store create(Store store) {
        log.info("Method create begin");
        if (store.getId() == null || store.getId() == 0L) {
            Store savedStore = storeRepository.save(store);
            log.info("Store " + savedStore.getId() + " is created");
            return savedStore;
        } else {
            IllegalArgumentException exception = new IllegalArgumentException("Error! New store id can only 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        }
    }

    @Transactional
    public List<Store> findAll() {
        log.info("Method findAll begin");
        List<Store> stores = storeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        log.info("Method findAll result set size = " + stores.size());
        return stores;
    }

    @Transactional
    public List<Store> getStoresByCityOrNameEquals(String cityName, String storeName, String sortingDirection){
        log.info("Method getStoresByCityOrNameEquals begin");
        if (sortingDirection == null) sortingDirection = "asc";

        if (cityName != null & storeName != null) {
            log.info("Condition where cityName and storeName not null is met");
            try {
                Sort sort = Sort.by(Sort.Direction.fromString(sortingDirection), "name");
                return storeRepository.getStoresByCityAndNameEquals(sort, cityName, storeName).orElseThrow(()-> {
                    NotFoundException exception = new NotFoundException("Error! Store not found by cityName " + cityName + ", and by storeName " + storeName);
                    log.error(exception.getMessage());
                    return exception;
                });
            } catch (IllegalArgumentException ex){
                BadRequestException exception = new BadRequestException("Error! Invalid parameter sortingDirection " + sortingDirection +". Valid parameter: ASC, DESC.");
                log.error(exception.getMessage());
                throw exception;
            }
        } else if (cityName != null & storeName == null) {
            log.info("Condition where cityName not null and storeName null is met");
            try{
                Sort sort =  Sort.by(Sort.Direction.fromString(sortingDirection), "name");
                return storeRepository.getStoresByCityEquals(sort, cityName).orElseThrow(()-> {
                    NotFoundException exception = new NotFoundException("Error! Store not found by cityName " + cityName);
                    log.error(exception.getMessage());
                    return exception;
                });
            } catch (IllegalArgumentException ex){
                BadRequestException exception = new BadRequestException("Error! Invalid parameter sortingDirection " + sortingDirection +". Valid parameter: ASC, DESC.");
                log.error(exception.getMessage());
                throw exception;
            }
        } else {
            log.info("Condition where storeName not null and cityName null is met");
            try {
                Sort sort =  Sort.by(Sort.Direction.fromString(sortingDirection), "city");
                return storeRepository.getStoresByNameEquals(sort, storeName).orElseThrow(()-> {
                    NotFoundException exception = new NotFoundException("Error! Store not found by storeName " + storeName);
                    log.error(exception.getMessage());
                    return exception;
                });
            } catch (IllegalArgumentException ex){
                BadRequestException exception = new BadRequestException("Error! Invalid parameter sortingDirection " + sortingDirection +". Valid parameter: ASC, DESC.");
                log.error(exception.getMessage());
                throw exception;
            }
        }
    }

    @Transactional
    public Store findById(Long id) {
        log.info("Method findById begin");
        return storeRepository.findById(id).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! Store not found with id " + id);
            log.error(exception.getMessage());
            return exception;
        });
    }

    @Transactional
    public Store update(Store store) {
        log.info("Method update begin");
        idValidation(store.getId());
        Store savedStore = storeRepository.save(store);
        log.info("Price " + savedStore.getId() + " is updated");
        return savedStore;
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("Method deleteById begin");
        idValidation(id);
        storeRepository.deleteById(id);
        log.info("Store " + id + " is deleted");
    }

   @Transactional
    public void idValidation(Long id) {
        log.info("Method idValidation begin");
        if (id == null || id == 0L ) {
            IllegalArgumentException exception = new IllegalArgumentException("Error! Store id cannot be 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        } else if (!storeRepository.existsById(id)){
            IllegalArgumentException exception = new IllegalArgumentException("Error! Store does not exist with this id - " + id);
            log.error(exception.getMessage());
            throw  exception;
        }
    }
}
