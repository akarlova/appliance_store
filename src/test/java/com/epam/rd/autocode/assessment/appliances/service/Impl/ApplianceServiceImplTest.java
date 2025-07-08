package com.epam.rd.autocode.assessment.appliances.service.Impl;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.repository.ApplianceRepository;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.ApplianceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplianceServiceImplTest {
    @Mock
    private ApplianceRepository repo;

    @Mock
    private ManufacturerRepository manufacturerRepo;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private ApplianceServiceImpl service;

    @Test
    void whenFindAll_thenReturnDtosWithManufacturerName() {
        // prepare entity with manufacturer
        Appliance entity = new Appliance();
        entity.setId(1L);
        Manufacturer m = new Manufacturer();
        m.setName("Acme");
        entity.setManufacturer(m);
        when(repo.findAll()).thenReturn(List.of(entity));
        // prepare mapping
        ApplianceDto dto = new ApplianceDto();
        when(mapper.map(entity, ApplianceDto.class)).thenReturn(dto);

        // call service
        List<ApplianceDto> result = service.findAll();

        // verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
        assertEquals("Acme", dto.getManufacturerName());
        verify(repo).findAll();
        verify(mapper).map(entity, ApplianceDto.class);
    }

    @Test
    void whenFindByIdExists_thenReturnDtoWithManufacturerName() {
        Appliance entity = new Appliance();
        entity.setId(2L);
        Manufacturer m = new Manufacturer();
        m.setName("GlobalTech");
        entity.setManufacturer(m);
        when(repo.findById(2L)).thenReturn(Optional.of(entity));
        ApplianceDto dto = new ApplianceDto();
        when(mapper.map(entity, ApplianceDto.class)).thenReturn(dto);

        ApplianceDto result = service.findById(2L);

        assertSame(dto, result);
        assertEquals("GlobalTech", result.getManufacturerName());
        verify(repo).findById(2L);
    }

    @Test
    void whenFindByIdNotExists_thenThrow() {
        when(repo.findById(3L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(3L));
        verify(repo).findById(3L);
    }

    @Test
    void whenCreate_thenSaveAndReturnDto() {
        // prepare dto input
        ApplianceDto dtoIn = new ApplianceDto();
        dtoIn.setManufacturerId(5L);
        // prepare entity mapping
        Appliance mappedEntity = new Appliance();
        when(mapper.map(dtoIn, Appliance.class)).thenReturn(mappedEntity);
        // prepare manufacturer
        Manufacturer m = new Manufacturer(); m.setId(5L); m.setName("Maker");
        when(manufacturerRepo.findById(5L)).thenReturn(Optional.of(m));
        // prepare save
        Appliance savedEntity = new Appliance(); savedEntity.setId(100L); savedEntity.setManufacturer(m);
        when(repo.save(mappedEntity)).thenReturn(savedEntity);
        // prepare output mapping
        ApplianceDto dtoOut = new ApplianceDto();
        when(mapper.map(savedEntity, ApplianceDto.class)).thenReturn(dtoOut);

        ApplianceDto result = service.create(dtoIn);

        assertSame(dtoOut, result);
        assertEquals("Maker", result.getManufacturerName());
        verify(mapper).map(dtoIn, Appliance.class);
        verify(manufacturerRepo).findById(5L);
        verify(repo).save(mappedEntity);
        verify(mapper).map(savedEntity, ApplianceDto.class);
    }

    @Test
    void whenUpdateExists_thenUpdateAndReturnDto() {
        ApplianceDto dtoIn = new ApplianceDto();
        dtoIn.setManufacturerId(7L);
        // existing entity
        Appliance existing = new Appliance(); existing.setId(10L);
        when(repo.findById(10L)).thenReturn(Optional.of(existing));
        // simulate mapping onto entity
        doNothing().when(mapper).map(dtoIn, existing);
        // manufacturer lookup
        Manufacturer m = new Manufacturer(); m.setId(7L); m.setName("UpdateCo");
        when(manufacturerRepo.findById(7L)).thenReturn(Optional.of(m));
        // saved entity
        Appliance updated = new Appliance(); updated.setId(10L); updated.setManufacturer(m);
        when(repo.save(existing)).thenReturn(updated);
        // output mapping
        ApplianceDto dtoOut = new ApplianceDto();
        when(mapper.map(updated, ApplianceDto.class)).thenReturn(dtoOut);

        ApplianceDto result = service.update(10L, dtoIn);

        assertSame(dtoOut, result);
        assertEquals("UpdateCo", result.getManufacturerName());
        verify(repo).findById(10L);
        verify(mapper).map(dtoIn, existing);
        verify(manufacturerRepo).findById(7L);
        verify(repo).save(existing);
        verify(mapper).map(updated, ApplianceDto.class);
    }

    @Test
    void whenUpdateNotExists_thenThrow() {
        ApplianceDto dtoIn = new ApplianceDto(); dtoIn.setManufacturerId(9L);
        when(repo.findById(11L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(11L, dtoIn));
        verify(repo).findById(11L);
    }

    @Test
    void whenDelete_thenInvokeRepo() {
        service.delete(4L);
        verify(repo).deleteById(4L);
    }

    @Test
    void whenFindLatest_thenReturnDtos() {
        Appliance entity = new Appliance();
        entity.setId(20L);

        when(repo.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(entity)));

        ApplianceDto dto = new ApplianceDto();
        when(mapper.map(entity, ApplianceDto.class)).thenReturn(dto);

        List<ApplianceDto> result = service.findLatest(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(dto, result.get(0));

        verify(repo).findAll(any(PageRequest.class));
    }
}
