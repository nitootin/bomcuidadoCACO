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

import com.example.demo.dtos.AlertasDTO;
import com.example.demo.services.AlertasService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping({"/alerta", "/alertas"})
@CrossOrigin(origins = "*")
public class AlertasController {

    private final AlertasService service;

    public AlertasController(AlertasService service) {
        this.service = service;
    }

    @Operation(
        summary = "Listar alertas",
        description = "Retorna uma lista paginada de alertas nao cancelados dos idosos vinculados ao cuidador autenticado"
    )
    @GetMapping("/listar_todos")
    public ResponseEntity<Page<AlertasDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(service.listarAtivosDoCuidador(getCuidadorId(authentication), pageable));
    }

    @Operation(
        summary = "Listar alertas do idoso autenticado",
        description = "Retorna alertas nao cancelados do idoso autenticado no app"
    )
    @GetMapping("/me")
    public ResponseEntity<Page<AlertasDTO>> listarMeusAlertas(
            @PageableDefault(size = 20, sort = "data_agendade") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(service.listarDoIdoso(getUsuarioId(authentication), pageable));
    }

    @Operation(
        summary = "Listar alertas por idoso",
        description = "Retorna alertas nao cancelados de um idoso vinculado ao cuidador autenticado"
    )
    @GetMapping("/idoso/{idosoId}")
    public ResponseEntity<Page<AlertasDTO>> listarPorIdoso(
            @PathVariable Integer idosoId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(service.listarPorIdoso(idosoId, getCuidadorId(authentication), pageable));
    }

    @Operation(
        summary = "Buscar alerta por ID",
        description = "Retorna os dados de um alerta de idoso vinculado ao cuidador autenticado"
    )
    @GetMapping("/listar/{id}")
    public ResponseEntity<AlertasDTO> buscarPorId(@PathVariable int id, Authentication authentication) {
        return ResponseEntity.ok(service.buscarPorId(id, getCuidadorId(authentication)));
    }

    @Operation(
        summary = "Cadastrar alerta",
        description = "Cria um novo alerta para um idoso vinculado ao cuidador autenticado"
    )
    @PostMapping("/cadastrar")
    public ResponseEntity<AlertasDTO> criar(@RequestBody AlertasDTO dto, Authentication authentication) {
        AlertasDTO criado = service.criar(dto, getCuidadorId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @Operation(
        summary = "Atualizar alerta",
        description = "Atualiza um alerta de idoso vinculado ao cuidador autenticado"
    )
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<AlertasDTO> atualizar(
            @PathVariable int id,
            @RequestBody AlertasDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(service.atualizar(id, dto, getCuidadorId(authentication)));
    }

    @Operation(
        summary = "Cancelar alerta",
        description = "Realiza a exclusao logica de um alerta de idoso vinculado ao cuidador autenticado"
    )
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable int id, Authentication authentication) {
        service.cancelar(id, getCuidadorId(authentication));
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Confirmar alerta",
        description = "Marca um alerta do idoso autenticado como realizado"
    )
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<AlertasDTO> confirmar(@PathVariable int id, Authentication authentication) {
        return ResponseEntity.ok(service.confirmar(id, getUsuarioId(authentication)));
    }

    private Integer getCuidadorId(Authentication authentication) {
        return getUsuarioId(authentication);
    }

    private Integer getUsuarioId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Integer id) {
            return id;
        }

        if (principal instanceof Number id) {
            return id.intValue();
        }

        return Integer.valueOf(principal.toString());
    }
}
