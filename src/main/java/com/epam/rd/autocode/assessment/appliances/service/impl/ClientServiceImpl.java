package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.ClientDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientProfileDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientProfileUpdateDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientRegistrationDto;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {
    private final ClientRepository repo;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public ClientServiceImpl(ClientRepository repo,
                             ModelMapper mapper,
                             PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<ClientDto> findAll() {
        return repo.findAll().stream()
                .map(e -> mapper.map(e, ClientDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ClientDto findById(Long id) {
        Client e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
        return mapper.map(e, ClientDto.class);
    }

    @Override
    @Transactional
    public ClientDto create(ClientDto dto) {
        Client entity = mapper.map(dto, Client.class);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        Client saved = repo.save(entity);
        return mapper.map(saved, ClientDto.class);
    }

    @Override
    @Transactional
    public ClientDto update(Long id, ClientDto dto) {
        Client entity = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setCard(dto.getCard());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return mapper.map(repo.save(entity), ClientDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    @Transactional
    public ClientDto register(ClientRegistrationDto dto) {
        Client entity = new Client();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        Client saved = repo.save(entity);
        return mapper.map(saved, ClientDto.class);
    }
    @Override
    public ClientProfileDto profileByEmail(String email) {
        Client c = repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + email));
        return mapper.map(c, ClientProfileDto.class);
    }

    @Override
    @Transactional
    public ClientProfileDto updateProfileByEmail(String email, ClientProfileUpdateDto dto) {
        Client c = repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + email));
        c.setName(dto.getName());
        c.setEmail(dto.getEmail());
        c.setPassword(passwordEncoder.encode(dto.getPassword()));
        Client updated = repo.save(c);
        return mapper.map(updated, ClientProfileDto.class);
    }
    @Override
    public ClientDto findByEmail(String email) {
        return repo.findByEmail(email)
                .map(e -> mapper.map(e, ClientDto.class))
                .orElseThrow(() -> new ResourceNotFoundException("Client not found by email " + email));
    }
}

