package com.meubairro.api.controller;

import com.meubairro.api.dto.request.EstabCreateRequest;
import com.meubairro.api.dto.request.EstabUpdateRequest;
import com.meubairro.api.dto.request.FiltroEstabRequest;
import com.meubairro.api.dto.response.EstabResponse;
import com.meubairro.api.dto.response.EstabResumeResponse;
import com.meubairro.api.service.EstabService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/estabalecimentos")
@RequiredArgsConstructor
public class EstabController {

    private EstabService service;

    @PostMapping
    public ResponseEntity<EstabResponse> create(@RequestBody EstabCreateRequest request){
        return ResponseEntity.ok(service.createEstab(request));
     }

     @GetMapping
    public  ResponseEntity<Page<EstabResumeResponse>> listar(
             @RequestParam(required = false) String name,
             @RequestParam(required = false) String categorySlug,
             @RequestParam(required = false) String address,
             @PageableDefault(size =12)Pageable pageable){
         FiltroEstabRequest filtro = new FiltroEstabRequest(name, categorySlug, address);

         return ResponseEntity.ok(service.listar(filtro, pageable));
     }

     @GetMapping("/{id}")
        public ResponseEntity<EstabResponse> buscarPorId(@PathVariable UUID id){
         return ResponseEntity.ok(service.buscarPorId(id));
     }

     //editar dados principais de um estabelecimento
     @PutMapping("/{id}")
    public ResponseEntity<EstabResponse> editar(@PathVariable UUID id, @RequestBody EstabUpdateRequest request){
         return ResponseEntity.ok(service.editar(id, request));
     }

     //ativar e desativar um estabelecimento
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable UUID id, @RequestParam Boolean active){
        service.alterarStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    //deletar permaneenteemente um estabelecimento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id){
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

}

