package com.meubairro.api.controller;

import com.meubairro.api.domain.estab.Estab;
import com.meubairro.api.dto.request.EstabRequest;
import com.meubairro.api.service.EstabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estabs")
public class EstabController {
    @Autowired
    private EstabService estabService;

    @PostMapping
    public ResponseEntity<Estab> create(@RequestParam("name") String name,
                                        @RequestParam("category") String category,
                                        @RequestParam("description") String description,
                                        @RequestParam("address") String address,
                                        @RequestParam("time") String time,
                                        @RequestParam("phone") Integer phone,
                                        @RequestParam("services") String services,
                                        @RequestParam("active") Boolean active,
                                        @RequestParam("createAt") Long createAt) {
        EstabRequest estabRequest = new EstabRequest(name, category, description, address, time, phone, services, active, createAt);
        Estab newEstab = this.estabService.createEstab(estabRequest);
        return ResponseEntity.ok(newEstab);
    }

}

