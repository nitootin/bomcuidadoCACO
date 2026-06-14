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

import com.example.demo.dtos.PrescricaoDTO;
import com.example.demo.services.PrescricaoService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/prescricao")
@CrossOrigin(origins = "*")
public class PrescricaoController {

    private final PrescricaoService service;

    public PrescricaoController(PrescricaoService service) {
        this.service = service;
    }

    @Operation(
        summary = "Listar prescricoes",
        description = "Retorna uma lista paginada de prescricoes ativas"
    )
    @GetMapping("/listar_todas")
    public ResponseEntity<Page<PrescricaoDTO>> listarTodas(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listarAtivas(pageable));
    }

    @Operation(
        summary = "Listar prescricoes por idoso",
        description = "Retorna uma lista paginada de prescricoes ativas de um idoso"
    )
    @GetMapping("/idoso/{idosoId}")
    public ResponseEntity<Page<PrescricaoDTO>> listarPorIdoso(
            @PathVariable Integer idosoId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listarPorIdoso(idosoId, pageable));
    }

    @Operation(
        summary = "Buscar prescricao por ID",
        description = "Retorna os dados de uma prescricao especifica com base no ID informado"
    )
    @GetMapping("/listar/{id}")
    public ResponseEntity<PrescricaoDTO> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(
        summary = "Cadastrar prescricao",
        description = "Cria uma nova prescricao com os dados enviados no corpo da requisicao"
    )
    @PostMapping("/cadastrar")
    public ResponseEntity<PrescricaoDTO> criar(@RequestBody PrescricaoDTO dto) {
        PrescricaoDTO criada = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @Operation(
        summary = "Atualizar prescricao",
        description = "Atualiza os dados de uma prescricao existente com base no ID informado"
    )
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<PrescricaoDTO> atualizar(@PathVariable int id, @RequestBody PrescricaoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @Operation(
        summary = "Deletar (inativar) prescricao",
        description = "Realiza a exclusao logica de uma prescricao com base no ID informado"
    )
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable int id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
