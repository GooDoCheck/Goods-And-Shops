package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PriceListDTO {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long productId;
    private BigDecimal currentPrice;
    private List<PriceDTO> priceHistoryList = new ArrayList<>();
}
