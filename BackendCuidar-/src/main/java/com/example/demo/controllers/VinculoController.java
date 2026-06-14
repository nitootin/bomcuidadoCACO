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
import com.example.demo.dtos.VinculoDTO;
import com.example.demo.services.VinculoService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/vinculo")
@CrossOrigin(origins = "*")
public class VinculoController {

    private final VinculoService service;

    public VinculoController(VinculoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar vínculos", description = "Retorna uma lista paginada de todos os vínculos")
    @GetMapping("/listar_todos")
    public ResponseEntity<Page<VinculoDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(pageable));
    }

    @Operation(summary = "Listar vínculos por idoso", description = "Retorna vínculos de um idoso específico")
    @GetMapping("/idoso/{idosoId}")
    public ResponseEntity<Page<VinculoDTO>> listarPorIdoso(
            @PathVariable Integer idosoId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listarPorIdoso(idosoId, pageable));
    }

    @Operation(summary = "Listar vínculos por cuidador", description = "Retorna vínculos de um cuidador específico")
    @GetMapping("/cuidador/{cuidadorId}")
    public ResponseEntity<Page<VinculoDTO>> listarPorCuidador(
            @PathVariable Integer cuidadorId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listarPorCuidador(cuidadorId, pageable));
    }

    @Operation(summary = "Buscar vínculo por ID", description = "Retorna os dados de um vínculo pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<VinculoDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Criar vínculo", description = "Cria um novo vínculo entre cuidador e idoso")
    @PostMapping("/cadastrar")
    public ResponseEntity<VinculoDTO> criar(@RequestBody VinculoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @Operation(summary = "Definir cuidador de emergência", description = "Define um cuidador vinculado como contato de emergência do idoso, removendo o anterior se houver")
    @PutMapping("/{id}/emergencia")
    public ResponseEntity<VinculoDTO> definirCuidadorEmergencia(@PathVariable Integer id) {
        return ResponseEntity.ok(service.definirCuidadorEmergencia(id));
    }

    @Operation(summary = "Buscar contato de emergência", description = "Retorna o contato do cuidador de emergência de um idoso")
    @GetMapping("/idoso/{idosoId}/contato-emergencia")
    public ResponseEntity<ContatoDTO> buscarContatoDeEmergencia(@PathVariable Integer idosoId) {
        return ResponseEntity.ok(service.buscarContatoDeEmergencia(idosoId));
    }

    @Operation(summary = "Deletar vínculo", description = "Remove o vínculo entre cuidador e idoso")
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}