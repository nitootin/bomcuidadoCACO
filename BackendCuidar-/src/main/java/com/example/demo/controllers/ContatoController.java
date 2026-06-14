package com.example.demo.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.ContatoDTO;
import com.example.demo.services.ContatoService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/contato")
@CrossOrigin(origins = "*")
public class ContatoController {

    private final ContatoService service;

    public ContatoController(ContatoService service) {
        this.service = service;
    }

    @Operation(
        summary = "Listar contatos",
        description = "Retorna uma lista paginada de contatos"
    )
    @GetMapping("/listar_todos")
    public ResponseEntity<Page<ContatoDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(pageable));
    }

    @Operation(
        summary = "Listar contatos por idoso",
        description = "Retorna uma lista paginada de contatos vinculados a um idoso específico"
    )
    @GetMapping("/listar/{idosoId}")
    public ResponseEntity<Page<ContatoDTO>> listarPorIdoso(
            @PathVariable Integer idosoId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listarPorIdoso(idosoId, pageable));
    }

    @Operation(
        summary = "Buscar contato por ID",
        description = "Retorna os dados de um contato pelo ID informado"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ContatoDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(
        summary = "Cadastrar contato",
        description = "Cria um novo contato com os dados informados"
    )
    @PostMapping("/cadastrar")
    public ResponseEntity<ContatoDTO> criar(@Valid @RequestBody ContatoDTO dto) {
        ContatoDTO criado = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @Operation(
        summary = "Atualizar contato",
        description = "Atualiza os dados de um contato existente"
    )
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<ContatoDTO> atualizar(@PathVariable Integer id, @Valid @RequestBody ContatoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @Operation(
        summary = "Deletar contato",
        description = "Remove um contato quando ele não estiver vinculado a cuidador ou idoso"
    )
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
