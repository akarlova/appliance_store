package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceDto;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApplianceService {
    List<ApplianceDto> findAll();
    ApplianceDto findById(Long id);
    ApplianceDto create(ApplianceDto dto);
    ApplianceDto update(Long id, ApplianceDto dto);
    void delete(Long id);
    List<ApplianceDto> findLatest(int count);
    Page<Appliance> findAll(Pageable pageable);
    Page<Appliance> search(String term, Pageable pageable);
}
