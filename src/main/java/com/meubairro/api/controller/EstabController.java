package com.meubairro.api.controller;

import com.meubairro.api.dto.request.AdminStatusRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.util.UUID;

@Controller
@RequestMapping({"/estabelecimentos"})
@RequiredArgsConstructor
public class EstabController {

    private final EstabService service;

    @GetMapping("/home")
    public ModelAndView home() {
        return new ModelAndView("forward:/home.html");
    }

    @GetMapping("/cadastro")
    public ModelAndView abrirCadastro() {
        return new ModelAndView("forward:/cadastro.html");
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<EstabResponse> create(@RequestBody EstabCreateRequest request){
        return ResponseEntity.ok(service.createEstab(request));
     }

    @GetMapping
    @ResponseBody
    public  ResponseEntity<Page<EstabResumeResponse>> listar(
             @RequestParam(required = false) String name,
             @RequestParam(required = false) String categorySlug,
             @RequestParam(required = false) String address,
             @PageableDefault(size =12)Pageable pageable){
         FiltroEstabRequest filtro = new FiltroEstabRequest(name, categorySlug, address);

         return ResponseEntity.ok(service.listar(filtro, pageable));
     }

    @GetMapping("/{id}")
    @ResponseBody
        public ResponseEntity<EstabResponse> buscarPorId(@PathVariable UUID id){
            return ResponseEntity.ok(service.buscarPorId(id));
    }

     //editar dados principais de um estabelecimento
    @PutMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('DONO')")
    public ResponseEntity<EstabResponse> editar(@PathVariable UUID id, @RequestBody EstabUpdateRequest request){
         return ResponseEntity.ok(service.editar(id, request));
     }

     //ativar e desativar um estabelecimento
    @PatchMapping("/{id}/status")
    @ResponseBody
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<Void> alterarStatusAdmin(@PathVariable UUID id, @RequestParam AdminStatusRequest request){
        service.alterarStatusAdmin(id, request.activeAdmin());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/todos")
    @ResponseBody
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<Page<EstabResumeResponse>> listarTodosParaAdmin(
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(service.listarTodosParaAdmin(pageable));
    }



    //deletar permaneenteemente um estabelecimento
    //@DeleteMapping("/{id}")
    //@ResponseBody
    //@PreAuthorize("hasRole('ADM')")
    //public ResponseEntity<Void> deletar(@PathVariable UUID id) {
      //  service.deletar(id);
       // return ResponseEntity.noContent().build();
   // }
}

