package com.example.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.example.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,property = "id")
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Subcategory subcategory;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Unit unit;
    @Column(nullable = false)
    private String manufacturer;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "product", orphanRemoval = true)
    private List<PriceList> priceList = new ArrayList<>();
}
