package com.example.demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.RelatorioDTO;
import com.example.demo.services.RelatorioService;

@RestController
@CrossOrigin(origins = "*")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping("/admin/relatorio")
    public ResponseEntity<RelatorioDTO> gerar() {
        return ResponseEntity.ok(service.gerar());
    }
}
