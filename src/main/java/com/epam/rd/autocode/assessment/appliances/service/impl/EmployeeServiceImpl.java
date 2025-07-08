package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.EmployeeDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.repository.EmployeeRepository;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository repo;
    private final ModelMapper mapper;

    public EmployeeServiceImpl(EmployeeRepository repo, ModelMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<EmployeeDto> findAll() {
        return repo.findAll().stream()
                .map(e -> {
                    EmployeeDto dto = mapper.map(e, EmployeeDto.class);
                    dto.setDepartment(e.getDepartment());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDto findById(Long id) {
        Employee e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        return mapper.map(e, EmployeeDto.class);
    }

    @Override
    @Transactional
    public EmployeeDto create(EmployeeDto dto) {
        Employee saved = repo.save(mapper.map(dto, Employee.class));
        return mapper.map(saved, EmployeeDto.class);
    }

    @Override
    @Transactional
    public EmployeeDto update(Long id, EmployeeDto dto) {
        Employee entity = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setDepartment(dto.getDepartment());
        Employee updated = repo.save(entity);
        return mapper.map(updated, EmployeeDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
