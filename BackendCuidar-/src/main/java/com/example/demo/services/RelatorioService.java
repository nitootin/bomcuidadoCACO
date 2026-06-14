package com.example.demo.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dtos.RelatorioDTO;
import com.example.demo.dtos.RelatorioDTO.ItemCuidadorDTO;
import com.example.demo.dtos.RelatorioDTO.ItemIdosoDTO;
import com.example.demo.dtos.RelatorioDTO.SecaoCuidadorDTO;
import com.example.demo.dtos.RelatorioDTO.SecaoIdosoDTO;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Idoso;
import com.example.demo.enums.Status;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.utils.TextoUtils;

@Service
public class RelatorioService {

    private final CuidadorRepository cuidadorRepository;
    private final IdosoRepository idosoRepository;

    public RelatorioService(
            CuidadorRepository cuidadorRepository,
            IdosoRepository idosoRepository) {
        this.cuidadorRepository = cuidadorRepository;
        this.idosoRepository = idosoRepository;
    }

    public RelatorioDTO gerar() {
        List<Cuidador> cuidadores = cuidadorRepository.findAll();
        List<Idoso> idosos = idosoRepository.findAll();

        return new RelatorioDTO(
                LocalDateTime.now(),
                montarSecaoCuidador(cuidadores),
                montarSecaoIdoso(idosos));
    }

    private SecaoCuidadorDTO montarSecaoCuidador(List<Cuidador> lista) {
        long ativos = lista.stream().filter(c -> c.getStatus() == Status.ATIVO).count();
        long inativos = lista.stream().filter(c -> c.getStatus() == Status.INATIVO).count();

        List<ItemCuidadorDTO> items = lista.stream()
                .map(c -> new ItemCuidadorDTO(
                        c.getId(),
                        TextoUtils.paraExibicao(c.getNome()),
                        c.getEmail(),
                        c.getCpf(),
                        c.getStatus().name()))
                .toList();

        return new SecaoCuidadorDTO(lista.size(), ativos, inativos, items);
    }

    private SecaoIdosoDTO montarSecaoIdoso(List<Idoso> lista) {
        long ativos = lista.stream().filter(i -> i.getStatus() == Status.ATIVO).count();
        long inativos = lista.stream().filter(i -> i.getStatus() == Status.INATIVO).count();

        List<ItemIdosoDTO> items = lista.stream()
                .map(i -> new ItemIdosoDTO(
                        i.getId(),
                        TextoUtils.paraExibicao(i.getNome()),
                        i.getCpf(),
                        i.getStatus().name()))
                .toList();

        return new SecaoIdosoDTO(lista.size(), ativos, inativos, items);
    }
}
