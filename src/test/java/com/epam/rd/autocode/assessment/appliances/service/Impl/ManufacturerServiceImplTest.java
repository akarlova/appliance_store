package com.epam.rd.autocode.assessment.appliances.service.Impl;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.ManufacturerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManufacturerServiceImplTest {
    private ManufacturerRepository repo;
    private ModelMapper mapper;
    private ManufacturerServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(ManufacturerRepository.class);
        mapper = mock(ModelMapper.class);
        service = new ManufacturerServiceImpl(repo, mapper);
    }

    @Test
    void findAll_returnsMappedList() {
        Manufacturer m1 = new Manufacturer(); m1.setId(1L); m1.setName("A");
        Manufacturer m2 = new Manufacturer(); m2.setId(2L); m2.setName("B");
        when(repo.findAll()).thenReturn(Arrays.asList(m1, m2));
        ManufacturerDto dto1 = new ManufacturerDto(1L, "A");
        ManufacturerDto dto2 = new ManufacturerDto(2L, "B");
        when(mapper.map(m1, ManufacturerDto.class)).thenReturn(dto1);
        when(mapper.map(m2, ManufacturerDto.class)).thenReturn(dto2);

        var result = service.findAll();
        assertEquals(2, result.size());
        assertSame(dto1, result.get(0));
        assertSame(dto2, result.get(1));
        verify(repo).findAll();
    }

    @Test
    void findById_exists_returnsDto() {
        Manufacturer m = new Manufacturer(); m.setId(5L); m.setName("X");
        when(repo.findById(5L)).thenReturn(Optional.of(m));
        ManufacturerDto dto = new ManufacturerDto(5L, "X");
        when(mapper.map(m, ManufacturerDto.class)).thenReturn(dto);

        var result = service.findById(5L);
        assertSame(dto, result);
        verify(repo).findById(5L);
    }

    @Test
    void findById_notExists_throws() {
        when(repo.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(10L));
    }

    @Test
    void create_mapsAndReturnsDto() {
        ManufacturerDto in = new ManufacturerDto(null, "New");
        Manufacturer entity = new Manufacturer();
        Manufacturer saved = new Manufacturer(); saved.setId(7L); saved.setName("New");
        ManufacturerDto out = new ManufacturerDto(7L, "New");

        when(mapper.map(in, Manufacturer.class)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(saved);
        when(mapper.map(saved, ManufacturerDto.class)).thenReturn(out);

        var result = service.create(in);
        assertSame(out, result);
        verify(repo).save(entity);
    }

    @Test
    void update_exists_updatesAndReturnsDto() {
        ManufacturerDto dto = new ManufacturerDto(null, "Updated");
        Manufacturer existing = new Manufacturer(); existing.setId(9L); existing.setName("Old");
        Manufacturer updated = new Manufacturer(); updated.setId(9L); updated.setName("Updated");
        ManufacturerDto out = new ManufacturerDto(9L, "Updated");

        when(repo.findById(9L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(updated);
        when(mapper.map(updated, ManufacturerDto.class)).thenReturn(out);

        var result = service.update(9L, dto);
        assertSame(out, result);
        assertEquals("Updated", existing.getName());
        verify(repo).findById(9L);
        verify(repo).save(existing);
    }

    @Test
    void update_notExists_throws() {
        ManufacturerDto dto = new ManufacturerDto(null, "X");
        when(repo.findById(11L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(11L, dto));
    }

    @Test
    void delete_invokesRepo() {
        service.delete(4L);
        verify(repo).deleteById(4L);
    }
}
