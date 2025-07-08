package com.epam.rd.autocode.assessment.appliances.service.Impl;

import com.epam.rd.autocode.assessment.appliances.dto.ClientDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientProfileDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientProfileUpdateDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientRegistrationDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClientServiceImplTest {
    private ClientRepository repo;
    private ModelMapper mapper;
    private PasswordEncoder encoder;
    private ClientServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(ClientRepository.class);
        mapper = mock(ModelMapper.class);
        encoder = mock(PasswordEncoder.class);
        service = new ClientServiceImpl(repo, mapper, encoder);
    }

    @Test
    void findAll_returnsMappedList() {
        Client c1 = new Client(); c1.setId(1L);
        Client c2 = new Client(); c2.setId(2L);
        when(repo.findAll()).thenReturn(Arrays.asList(c1, c2));
        ClientDto dto1 = new ClientDto(1L, null, null, null, null);
        ClientDto dto2 = new ClientDto(2L, null, null, null, null);
        when(mapper.map(c1, ClientDto.class)).thenReturn(dto1);
        when(mapper.map(c2, ClientDto.class)).thenReturn(dto2);

        var list = service.findAll();
        assertEquals(2, list.size());
        assertSame(dto1, list.get(0));
        assertSame(dto2, list.get(1));
        verify(repo).findAll();
    }

    @Test
    void findById_exists() {
        Client c = new Client(); c.setId(5L);
        when(repo.findById(5L)).thenReturn(Optional.of(c));
        ClientDto dto = new ClientDto(5L, null, null, null, null);
        when(mapper.map(c, ClientDto.class)).thenReturn(dto);

        var result = service.findById(5L);
        assertSame(dto, result);
    }

    @Test
    void findById_notExists_throws() {
        when(repo.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(10L));
    }

    @Test
    void create_encodesPassword_andMaps() {
        ClientDto in = new ClientDto(null, "name", "e@mail", "pass", "card");
        Client entity = new Client();
        when(mapper.map(in, Client.class)).thenReturn(entity);
        when(encoder.encode("pass")).thenReturn("enc");
        Client saved = new Client(); saved.setId(7L); saved.setPassword("enc");
        when(repo.save(entity)).thenReturn(saved);
        ClientDto out = new ClientDto(7L, null, null, null, null);
        when(mapper.map(saved, ClientDto.class)).thenReturn(out);

        var result = service.create(in);
        assertSame(out, result);
        assertEquals("enc", entity.getPassword());
    }

    @Test
    void update_exists_withAndWithoutPassword() {
        Client existing = new Client(); existing.setId(8L);
        when(repo.findById(8L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);
        ClientDto dto = new ClientDto(null, "n", "e@e", "", "card");
        ClientDto mapped = new ClientDto(8L, null, null, null, null);
        when(mapper.map(existing, ClientDto.class)).thenReturn(mapped);

        var noPass = service.update(8L, dto);
        assertSame(mapped, noPass);
        verify(encoder, never()).encode(any());

        // with password
        dto.setPassword("new");
        when(encoder.encode("new")).thenReturn("enc2");
        service.update(8L, dto);
        assertEquals("enc2", existing.getPassword());
    }

    @Test
    void update_notExists_throws() {
        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(9L, new ClientDto()));
    }

    @Test
    void delete_invokesRepo() {
        service.delete(4L);
        verify(repo).deleteById(4L);
    }

    @Test
    void register_mapsAndEncodes() {
        ClientRegistrationDto in = new ClientRegistrationDto("n","e@e","pw");
        when(encoder.encode("pw")).thenReturn("e_pw");
        Client saved = new Client(); saved.setId(11L); saved.setPassword("e_pw");
        when(repo.save(any(Client.class))).thenReturn(saved);
        ClientDto out = new ClientDto(11L, null, null, null, null);
        when(mapper.map(saved, ClientDto.class)).thenReturn(out);

        var res = service.register(in);
        assertSame(out, res);
    }

    @Test
    void profileByEmail_exists() {
        Client c = new Client(); c.setEmail("a@b");
        when(repo.findByEmail("a@b")).thenReturn(Optional.of(c));
        ClientProfileDto dto = new ClientProfileDto();
        when(mapper.map(c, ClientProfileDto.class)).thenReturn(dto);

        assertSame(dto, service.profileByEmail("a@b"));
    }

    @Test
    void profileByEmail_notExists_throws() {
        when(repo.findByEmail("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.profileByEmail("x"));
    }

    @Test
    void updateProfileByEmail_exists() {
        Client c = new Client(); c.setEmail("old");
        when(repo.findByEmail("old")).thenReturn(Optional.of(c));
        when(encoder.encode("pw2")).thenReturn("e2");
        ClientProfileUpdateDto upd = new ClientProfileUpdateDto("n","new@e","pw2", null);
        Client saved = new Client(); saved.setEmail("new@e");
        when(repo.save(c)).thenReturn(saved);
        ClientProfileDto pd = new ClientProfileDto();
        when(mapper.map(saved, ClientProfileDto.class)).thenReturn(pd);

        assertSame(pd, service.updateProfileByEmail("old", upd));
    }

    @Test
    void updateProfileByEmail_notExists_throws() {
        when(repo.findByEmail("none")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateProfileByEmail("none", new ClientProfileUpdateDto()));
    }

    @Test
    void findByEmail_exists() {
        Client c = new Client(); c.setEmail("aa");
        when(repo.findByEmail("aa")).thenReturn(Optional.of(c));
        ClientDto dto = new ClientDto();
        when(mapper.map(c, ClientDto.class)).thenReturn(dto);

        assertSame(dto, service.findByEmail("aa"));
    }

    @Test
    void findByEmail_notExists_throws() {
        when(repo.findByEmail("bb")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findByEmail("bb"));
    }
}