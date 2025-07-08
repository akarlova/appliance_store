package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ClientDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientProfileDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientProfileUpdateDto;
import com.epam.rd.autocode.assessment.appliances.dto.ClientRegistrationDto;

import java.util.List;

public interface ClientService {
    List<ClientDto> findAll();
    ClientDto findById(Long id);
    ClientDto create(ClientDto dto);
    ClientDto update(Long id, ClientDto dto);
    void delete(Long id);
    ClientDto register(ClientRegistrationDto dto);
    ClientProfileDto profileByEmail(String email);
    ClientProfileDto updateProfileByEmail(String email, ClientProfileUpdateDto dto);
    ClientDto findByEmail(String email);
}
