package com.example.entity.dto;

import com.example.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private Long subcategoryId;
    private String name;
    private String brand;
    private Integer quantity;
    private Unit unit;
    private String manufacturer;
    private ArrayList<Long> PriceListsId;
}


