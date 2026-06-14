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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.CuidadorDTO;
import com.example.demo.services.CuidadorService;

@RestController
@RequestMapping("/cuidador")
@CrossOrigin(origins = "*")
public class CuidadorController {

    private final CuidadorService service;

    public CuidadorController(CuidadorService service) {
        this.service = service;
    }

    @GetMapping("/listar_todos")
    public ResponseEntity<Page<CuidadorDTO>> listarTodos(
            @RequestParam(required = false) String cpf,
            Authentication authentication,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        if (isInstituicao(authentication)) {
            Integer instituicaoId = (Integer) authentication.getPrincipal();
            return ResponseEntity.ok(service.listarAtivosPorInstituicao(instituicaoId, cpf, pageable));
        }

        return ResponseEntity.ok(service.listarAtivos(pageable));
    }

    @GetMapping("/listar/{id}")
    public ResponseEntity<CuidadorDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<CuidadorDTO> criar(@RequestBody CuidadorDTO dto) {
        CuidadorDTO criado = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<CuidadorDTO> atualizar(@PathVariable Integer id, @RequestBody CuidadorDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PutMapping("/reativar/{id}")
    public ResponseEntity<CuidadorDTO> reativar(@PathVariable Integer id, @RequestBody(required = false) CuidadorDTO dto) {
        return ResponseEntity.ok(service.reativar(id, dto));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    private boolean isInstituicao(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_INSTITUICAO".equals(authority.getAuthority()));
    }
}
