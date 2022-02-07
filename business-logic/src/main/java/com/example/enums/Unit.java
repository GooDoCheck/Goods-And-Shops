package com.example.enums;

public enum Unit {

    MILLILITER("мл"),
    LITRE("л"),
    GRAM("гр"),
    KILOGRAM("кг"),
    PIECE("шт");


    private final String abbreviation;

    Unit(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
