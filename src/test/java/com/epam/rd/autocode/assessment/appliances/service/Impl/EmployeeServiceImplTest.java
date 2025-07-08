package com.epam.rd.autocode.assessment.appliances.service.Impl;

import com.epam.rd.autocode.assessment.appliances.dto.EmployeeDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.repository.EmployeeRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {
    private EmployeeRepository repo;
    private ModelMapper mapper;
    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(EmployeeRepository.class);
        mapper = mock(ModelMapper.class);
        service = new EmployeeServiceImpl(repo, mapper);
    }

    @Test
    void findAll_returnsMappedList_withDepartment() {
        // given
        Employee e1 = new Employee(); e1.setId(1L); e1.setDepartment("DeptA");
        Employee e2 = new Employee(); e2.setId(2L); e2.setDepartment("DeptB");
        when(repo.findAll()).thenReturn(Arrays.asList(e1, e2));

        EmployeeDto dto1 = new EmployeeDto();
        EmployeeDto dto2 = new EmployeeDto();
        when(mapper.map(e1, EmployeeDto.class)).thenReturn(dto1);
        when(mapper.map(e2, EmployeeDto.class)).thenReturn(dto2);

        // when
        List<EmployeeDto> result = service.findAll();

        // then
        assertSame(dto1, result.get(0));
        assertSame(dto2, result.get(1));
        assertEquals("DeptA", dto1.getDepartment());
        assertEquals("DeptB", dto2.getDepartment());
        verify(repo).findAll();
    }

    @Test
    void findById_exists_returnsDto() {
        Employee e = new Employee(); e.setId(5L);
        when(repo.findById(5L)).thenReturn(Optional.of(e));
        EmployeeDto dto = new EmployeeDto(5L, "Name", "email", "pass", "Dept");
        when(mapper.map(e, EmployeeDto.class)).thenReturn(dto);

        EmployeeDto result = service.findById(5L);
        assertSame(dto, result);
    }

    @Test
    void findById_notExists_throws() {
        when(repo.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(10L));
    }

    @Test
    void create_savesAndReturnsDto() {
        EmployeeDto in = new EmployeeDto(null, "n", "e@e", "pw", "Dept");
        Employee entity = new Employee();
        when(mapper.map(in, Employee.class)).thenReturn(entity);
        Employee saved = new Employee(); saved.setId(7L);
        when(repo.save(entity)).thenReturn(saved);
        EmployeeDto out = new EmployeeDto(7L, null, null, null, null);
        when(mapper.map(saved, EmployeeDto.class)).thenReturn(out);

        EmployeeDto result = service.create(in);
        assertSame(out, result);
        verify(repo).save(entity);
    }

    @Test
    void update_exists_updatesFieldsAndReturnsDto() {
        Employee existing = new Employee(); existing.setId(8L); existing.setDepartment("Old");
        when(repo.findById(8L)).thenReturn(Optional.of(existing));
        EmployeeDto dto = new EmployeeDto(null, "NewName", "new@e", "newpw", "NewDept");
        Employee updated = new Employee(); updated.setId(8L);
        when(repo.save(existing)).thenReturn(updated);
        EmployeeDto out = new EmployeeDto(8L, null, null, null, null);
        when(mapper.map(updated, EmployeeDto.class)).thenReturn(out);

        EmployeeDto result = service.update(8L, dto);
        assertSame(out, result);
        assertEquals("NewName", existing.getName());
        assertEquals("new@e", existing.getEmail());
        assertEquals("NewDept", existing.getDepartment());
    }

    @Test
    void update_notExists_throws() {
        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(9L, new EmployeeDto()));
    }

    @Test
    void delete_invokesRepo() {
        service.delete(4L);
        verify(repo).deleteById(4L);
    }
}

