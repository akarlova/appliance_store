package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.repository.ApplianceRepository;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ApplianceServiceImpl implements ApplianceService {
    private final ApplianceRepository repo;
    private final ManufacturerRepository manufacturerRepo;
    private final ModelMapper mapper;

    public ApplianceServiceImpl(ApplianceRepository repo,ManufacturerRepository manufacturerRepo,
                                ModelMapper mapper) {
        this.repo = repo;
        this.manufacturerRepo = manufacturerRepo;
        this.mapper = mapper;
    }

    @Override
    public List<ApplianceDto> findAll() {
        return repo.findAll().stream()
                .map(e -> {
                    ApplianceDto dto = mapper.map(e, ApplianceDto.class);
                    dto.setManufacturerName(e.getManufacturer().getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ApplianceDto findById(Long id) {
        Appliance e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance not found: " + id));
        ApplianceDto dto = mapper.map(e, ApplianceDto.class);
        dto.setManufacturerName(e.getManufacturer().getName());
        return dto;
    }

    @Override
    @Transactional
    public ApplianceDto create(ApplianceDto dto) {
        Appliance entity = mapper.map(dto, Appliance.class);
        var man = manufacturerRepo.findById(dto.getManufacturerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manufacturer not found: " + dto.getManufacturerId()));
        entity.setManufacturer(man);
        Appliance saved = repo.save(entity);
        ApplianceDto out = mapper.map(saved, ApplianceDto.class);
        out.setManufacturerName(man.getName());
        return out;
    }

    @Override
    @Transactional
    public ApplianceDto update(Long id, ApplianceDto dto) {
        Appliance entity = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found: " + id));
        mapper.map(dto, entity);
        var man = manufacturerRepo.findById(dto.getManufacturerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manufacturer not found: " + dto.getManufacturerId()));
        entity.setManufacturer(man);
        Appliance updated = repo.save(entity);
        ApplianceDto out = mapper.map(updated, ApplianceDto.class);
        out.setManufacturerName(man.getName());
        return out;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
    @Override
    public List<ApplianceDto> findLatest(int count) {
        return repo.findAll(PageRequest.of(0, count, Sort.by("id").descending()))
                .stream()
                .map(a -> mapper.map(a, ApplianceDto.class))
                .collect(Collectors.toList());
    }
    @Override
    public Page<Appliance> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    public Page<Appliance> search(String term, Pageable pageable) {
        return repo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrManufacturer_NameContainingIgnoreCase(
                term, term, term, pageable
        );
    }
}
