package com.example.demo.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.RemedioDTO;
import com.example.demo.services.RemedioService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/remedio")
@CrossOrigin(origins = "*")
public class RemedioController {

    private final RemedioService service;

    public RemedioController(RemedioService service) {
        this.service = service;
    }

    @Operation(
        summary = "Listar remédios",
        description = "Retorna uma lista paginada de remédios ativos ordenados por nome"
    )
    @GetMapping("/listar_todas")
    public ResponseEntity<Page<RemedioDTO>> listarTodas(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(service.listarAtivas(getCuidadorId(authentication), pageable));
    }

    @Operation(
        summary = "Buscar remédio por ID",
        description = "Retorna os dados de um remédio específico com base no ID informado"
    )
    @GetMapping("/listar/{id}")
    public ResponseEntity<RemedioDTO> buscarPorId(@PathVariable int id, Authentication authentication) {
        return ResponseEntity.ok(service.buscarPorId(id, getCuidadorId(authentication)));
    }

    @Operation(
        summary = "Cadastrar remédio",
        description = "Cria um novo remédio com os dados enviados no corpo da requisição"
    )
    @PostMapping("/cadastrar")
    public ResponseEntity<RemedioDTO> criar(@RequestBody RemedioDTO dto, Authentication authentication) {
        RemedioDTO criada = service.criar(dto, getCuidadorId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @Operation(
        summary = "Atualizar remédio",
        description = "Atualiza os dados de um remédio existente com base no ID informado"
    )
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<RemedioDTO> atualizar(
            @PathVariable int id,
            @RequestBody RemedioDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(service.atualizar(id, dto, getCuidadorId(authentication)));
    }

    @Operation(
        summary = "Deletar (inativar) remédio",
        description = "Realiza a exclusão lógica (inativação) de um remédio com base no ID informado"
    )
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable int id, Authentication authentication) {
        service.inativar(id, getCuidadorId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    private Integer getCuidadorId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }
}
