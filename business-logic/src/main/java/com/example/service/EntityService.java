package com.example.service;

import java.util.List;

public interface EntityService<T, S> {
    S convertToDTO(T entity);
    T convertFromDTO(S dto);
    List<S> listConverterToDTO(List<T> list);
    S create(S dto);
    List<T> findAll();
    T findById(Long id);
    S update(S dto);
    void deleteById(Long id);
    void idValidation(Long id);
}
