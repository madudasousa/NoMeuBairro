package com.meubairro.api.controller;

import com.meubairro.api.dto.request.CategoryRequest;
import com.meubairro.api.dto.response.CategoryResponse;
import com.meubairro.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> criar(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listar() {
        return ResponseEntity.ok(categoryService.listar());
    }

    //buscar categoria pelo slug
    @GetMapping("/{slug}")
    public ResponseEntity<CategoryResponse> buscarPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.buscarPorSlug(slug));
    }

    //editar o nome de uma categoria existente
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> editar(
            @PathVariable UUID id,
            @RequestBody CategoryRequest request){
        return ResponseEntity.ok(categoryService.editar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        categoryService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
