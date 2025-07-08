package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerDto;

import java.util.List;

public interface ManufacturerService {
    List<ManufacturerDto> findAll();
    ManufacturerDto findById(Long id);
    ManufacturerDto create(ManufacturerDto dto);
    ManufacturerDto update(Long id, ManufacturerDto dto);
    void delete(Long id);
}
