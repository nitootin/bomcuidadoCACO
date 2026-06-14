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

import com.example.demo.dtos.AdministradorDTO;
import com.example.demo.services.AdministradorService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/administrador")
@CrossOrigin(origins = "*")
public class AdministradorController {

    private final AdministradorService service;

    public AdministradorController(AdministradorService service) {
        this.service = service;
    }

    @Operation(
        summary = "Listar administradores",
        description = "Retorna uma lista paginada de administradores ativos ordenados por nome"
    )
    @GetMapping("/listar_todas")
    public ResponseEntity<Page<AdministradorDTO>> listarTodas(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listarAtivos(pageable));
    }

    @Operation(
        summary = "Buscar administrador por ID",
        description = "Retorna os dados de um administrador específico com base no ID informado"
    )
    @GetMapping("/listar/{id}")
    public ResponseEntity<AdministradorDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(
        summary = "Cadastrar administrador",
        description = "Cria um novo administrador com os dados enviados no corpo da requisição"
    )
    @PostMapping("/cadastrar")
    public ResponseEntity<AdministradorDTO> criar(@RequestBody AdministradorDTO dto) {
        AdministradorDTO criado = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @Operation(
        summary = "Atualizar administrador",
        description = "Atualiza os dados de um administrador existente com base no ID informado"
    )
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<AdministradorDTO> atualizar(@PathVariable Integer id, @RequestBody AdministradorDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @Operation(
        summary = "Deletar (inativar) administrador",
        description = "Realiza a exclusão lógica (inativação) de um administrador com base no ID informado"
    )
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
