package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ManufacturerServiceImpl implements ManufacturerService {
    private final ManufacturerRepository repo;
    private final ModelMapper mapper;

    public ManufacturerServiceImpl(ManufacturerRepository repo, ModelMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<ManufacturerDto> findAll() {
        return repo.findAll().stream()
                .map(e -> mapper.map(e, ManufacturerDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ManufacturerDto findById(Long id) {
        Manufacturer e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found: " + id));
        return mapper.map(e, ManufacturerDto.class);
    }

    @Override
    @Transactional
    public ManufacturerDto create(ManufacturerDto dto) {
        Manufacturer saved = repo.save(mapper.map(dto, Manufacturer.class));
        return mapper.map(saved, ManufacturerDto.class);
    }

    @Override
    @Transactional
    public ManufacturerDto update(Long id, ManufacturerDto dto) {
        Manufacturer entity = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found: " + id));
        entity.setName(dto.getName());
        Manufacturer updated = repo.save(entity);
        return mapper.map(updated, ManufacturerDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
