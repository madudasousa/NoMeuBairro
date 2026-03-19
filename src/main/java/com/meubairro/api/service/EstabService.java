package com.meubairro.api.service;

import com.meubairro.api.domain.estab.Estab;
import com.meubairro.api.dto.request.EstabRequest;
import com.meubairro.api.repositories.EstabRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EstabService {

    @Autowired
    private EstabRepository repository;

    public Estab createEstab(EstabRequest data){

        Estab newEstab = new Estab();
        newEstab.setName(data.name());
        newEstab.setCategory(data.category());
        newEstab.setDescription(data.description());
        newEstab.setAddress(data.address());
        newEstab.setTime(data.time());
        newEstab.setPhone(data.phone());
        newEstab.setServices(data.services());
        newEstab.setActive(data.active());
        newEstab.setCreateAt(data.createAt());

        repository.save(newEstab);
        return newEstab;
    }

    public Estab findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estabelecimento nao encontrado: " + id));
    }

}
