package com.example.service;

import com.example.entity.PriceList;
import com.example.entity.Product;
import com.example.entity.dto.ProductDTO;
import com.example.exceptions.NotFoundException;
import com.example.repository.IProductRepository;
import com.example.repository.ISubcategoryRepository;
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
public class ProductService implements EntityService<Product, ProductDTO> {
    private final IProductRepository productRepository;
    private final ISubcategoryRepository subcategoryRepository;
    @Getter
    @Setter
    private SubcategoryService subcategoryService;
    @Getter
    @Setter
    private PriceListService priceListService;

    @Autowired
    public ProductService(IProductRepository productRepository, ISubcategoryRepository subcategoryRepository) {
        this.productRepository = productRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    @Override
    public ProductDTO convertToDTO(Product product) {
        log.info("Method convertToDTO begin");
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setSubcategoryId(product.getSubcategory().getId());
        productDTO.setName(product.getName());
        productDTO.setBrand(product.getBrand());
        productDTO.setQuantity(product.getQuantity());
        productDTO.setUnit(product.getUnit());
        productDTO.setManufacturer(product.getManufacturer());

        ArrayList<Long> priceListsId = new ArrayList<>();
        if (product.getPriceList() != null){
            for (PriceList priceList : product.getPriceList()){
                priceListsId.add(priceList.getId());
            }
            productDTO.setPriceListsId(priceListsId);
        } else {
            productDTO.setPriceListsId(null);
        }
        return productDTO;
    }

    @Transactional
    @Override
    public Product convertFromDTO(ProductDTO productDTO) {
        log.info("Method convertFromDTO begin");
        Product product = new Product();
        product.setId(productDTO.getId());

        subcategoryService.idValidation(productDTO.getSubcategoryId());
        product.setSubcategory(subcategoryRepository.findById(productDTO.getSubcategoryId()).orElseThrow(() ->{
            NotFoundException exception = new NotFoundException("Error! Subcategory not found with id " + productDTO.getSubcategoryId());
            log.error(exception.getMessage());
            return exception;
        }));

        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setQuantity(productDTO.getQuantity());
        product.setUnit(productDTO.getUnit());
        product.setManufacturer(productDTO.getManufacturer());
        product.setPriceList(null);
        return product;
    }

    @Override
    public List<ProductDTO> listConverterToDTO(List<Product> products) {
        log.info("Method listConverterToDTO begin");
        List<ProductDTO> productDTOS = new ArrayList<>();
        for (Product product : products) {
            productDTOS.add(convertToDTO(product));
        }
        return productDTOS;
    }

    @Transactional
    @Override
    public ProductDTO create(ProductDTO productDTO) {
        log.info("Method create begin");
        if (productDTO.getId() == null || productDTO.getId() != 0L) {
            Product savedProduct = productRepository.save(convertFromDTO(productDTO));
            log.info("Product " + savedProduct.getId() + " is created");
            return convertToDTO(savedProduct);
        } else {
            IllegalArgumentException exception = new IllegalArgumentException("Error! New priceList id can only 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        }
    }

    @Transactional
    @Override
    public List<Product> findAll() {
        log.info("Method findAll begin");
        List<Product> products =  productRepository.findAll();
        log.info("Method findAll result set size = " + products.size());
        return products;
    }

    @Transactional
    public List<Product> findProductsByKeyword(String keyword, Sort sort) {
        log.info("Method findProductsByKeyword begin");
        if (keyword == null || keyword.isEmpty()) {
            log.info("Condition where keyword null or empty is met");
            return productRepository.findAll(sort);
        } else {
            log.info("Condition where keyword not null or not empty is met");
            return productRepository.findAllProductByKeywordContains(keyword, sort)
                    .orElseThrow(()-> {
                        NotFoundException exception = new NotFoundException("Error! Product not found by keyword " + keyword);
                        log.error(exception.getMessage());
                        return exception;
                    });
        }
    }

    @Transactional
    public List<Product> findProductsByCategoryAndByKeyword(String categoryName, String keyword, Sort sort) {
        log.info("Method findProductsByCategoryAndByKeyword begin");
        if (keyword == null || keyword.isEmpty()) {
            log.info("Condition where keyword null or empty is met");
            return productRepository.findAllProductByCategoryName(categoryName, sort)
                    .orElseThrow(() -> {
                        NotFoundException exception = new NotFoundException("Error! Product not found by category name " + categoryName);
                        log.error(exception.getMessage());
                        return exception;
                    });
        } else {
            log.info("Condition where keyword not null or not empty is met");
            return productRepository.findAllProductByKeywordContainsAndByCategoryName(categoryName, keyword, sort)
                    .orElseThrow(() -> {
                        NotFoundException exception = new NotFoundException("Error! Product not found by category name " + categoryName + ", and by keyword " + keyword);
                        log.error(exception.getMessage());
                        return exception;
                    });
        }
    }

    @Transactional
    public List<Product> findProductsBySubcategoryAndByKeyword(String subcategoryName, String keyword, Sort sort) {
        log.info("Method findProductsBySubcategoryAndByKeyword begin");
        if (keyword == null || keyword.isEmpty()) {
            log.info("Condition where keyword null or empty is met");
            return productRepository.findAllProductBySubcategoryName(subcategoryName, sort)
                    .orElseThrow(() -> {
                        NotFoundException exception = new NotFoundException("Error! Product not found by category name " + subcategoryName);
                        log.error(exception.getMessage());
                        return exception;
                    });
        } else {
            log.info("Condition where keyword not null or not empty is met");
            return productRepository.findAllProductByKeywordContainsAndBySubcategoryName(subcategoryName, keyword, sort)
                    .orElseThrow(() -> {
                        NotFoundException exception = new NotFoundException("Error! Product not found by category name " + subcategoryName + ", and by keyword " + keyword);
                        log.error(exception.getMessage());
                        return exception;
                    });
        }
    }

    @Transactional
    @Override
    public Product findById(Long id) {
        log.info("Method findById begin");
        return productRepository.findById(id).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! Product not found with id " + id);
            log.error(exception.getMessage());
            return exception;
        });
    }

    @Transactional
    @Override
    public ProductDTO update(ProductDTO productDTO) {
        log.info("Method update begin");
        idValidation(productDTO.getId());
        Product product = convertFromDTO(productDTO);
        product.setPriceList(productRepository.findById(product.getId()).orElseThrow(() -> {
            NotFoundException exception = new NotFoundException("Error! Product not found with id " + product.getId());
            log.error(exception.getMessage());
            return exception;
        }).getPriceList());

        ArrayList<PriceList> priceLists = new ArrayList<>();
        for (Long priceListsId : productDTO.getPriceListsId()){
            priceLists.add(priceListService.findById(priceListsId));
        }
        product.setPriceList(priceLists);
        Product savedProduct = productRepository.save(product);
        log.info("Product " + savedProduct.getId() + " is updated");
        return convertToDTO(savedProduct);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        log.info("Method deleteById begin");
        idValidation(id);
        productRepository.deleteById(id);
        log.info("Product " + id + " is deleted");
    }

    @Transactional
    @Override
    public void idValidation(Long id) {
        log.info("Method idValidation begin");
        if (id == null || id == 0L ) {
            IllegalArgumentException exception = new IllegalArgumentException("Error! Product id cannot be 0 or null");
            log.error(exception.getMessage());
            throw  exception;
        } else if (!productRepository.existsById(id)){
            IllegalArgumentException exception = new IllegalArgumentException("Error! Product does not exist with this id - " + id);
            log.error(exception.getMessage());
            throw  exception;
        }
    }

}
