package com.example.demo.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dtos.RelatorioDTO;
import com.example.demo.dtos.RelatorioDTO.ItemCuidadorDTO;
import com.example.demo.dtos.RelatorioDTO.ItemCuidadorInstituicaoDTO;
import com.example.demo.dtos.RelatorioDTO.ItemIdosoDTO;
import com.example.demo.dtos.RelatorioDTO.ItemIdosoInstituicaoDTO;
import com.example.demo.dtos.RelatorioDTO.ItemInstituicaoDTO;
import com.example.demo.dtos.RelatorioDTO.RelatorioInstituicaoDTO;
import com.example.demo.dtos.RelatorioDTO.SecaoCuidadorDTO;
import com.example.demo.dtos.RelatorioDTO.SecaoCuidadorInstituicaoDTO;
import com.example.demo.dtos.RelatorioDTO.SecaoIdosoDTO;
import com.example.demo.dtos.RelatorioDTO.SecaoIdosoInstituicaoDTO;
import com.example.demo.dtos.RelatorioDTO.SecaoInstituicaoDTO;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Instituicao;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.InstituicaoRepository;
import com.example.demo.utils.TextoUtils;

@Service
public class RelatorioService {

    private final InstituicaoRepository instituicaoRepository;
    private final CuidadorRepository cuidadorRepository;
    private final IdosoRepository idosoRepository;

    public RelatorioService(
            InstituicaoRepository instituicaoRepository,
            CuidadorRepository cuidadorRepository,
            IdosoRepository idosoRepository) {
        this.instituicaoRepository = instituicaoRepository;
        this.cuidadorRepository = cuidadorRepository;
        this.idosoRepository = idosoRepository;
    }

    // ── Relatório do Administrador ──────────────────────────────────────────────

    public RelatorioDTO gerar() {
        List<Instituicao> instituicoes = instituicaoRepository.findAll();
        List<Cuidador> cuidadores = cuidadorRepository.findAll();
        List<Idoso> idosos = idosoRepository.findAll();

        return new RelatorioDTO(
                LocalDateTime.now(),
                montarSecaoInstituicao(instituicoes),
                montarSecaoCuidador(cuidadores),
                montarSecaoIdoso(idosos));
    }

    private SecaoInstituicaoDTO montarSecaoInstituicao(List<Instituicao> lista) {
        long ativas = lista.stream().filter(i -> i.getStatus() == Status.ATIVO).count();
        long inativas = lista.stream().filter(i -> i.getStatus() == Status.INATIVO).count();

        List<ItemInstituicaoDTO> items = lista.stream()
                .map(i -> new ItemInstituicaoDTO(
                        i.getId(),
                        TextoUtils.paraExibicao(i.getNome()),
                        i.getCnpj(),
                        i.getEmail(),
                        TextoUtils.paraExibicao(i.getRua()),
                        TextoUtils.paraExibicao(i.getBairro()),
                        TextoUtils.paraUf(i.getUf()),
                        i.getStatus().name()))
                .toList();

        return new SecaoInstituicaoDTO(lista.size(), ativas, inativas, items);
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
                        c.getStatus().name(),
                        c.getInstituicao() != null ? TextoUtils.paraExibicao(c.getInstituicao().getNome()) : null))
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

    // ── Relatório da Instituição ────────────────────────────────────────────────

    public RelatorioInstituicaoDTO gerarInstituicao(Integer instituicaoId) {
        instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", instituicaoId.longValue()));

        List<Cuidador> cuidadores = cuidadorRepository.findByInstituicaoId(instituicaoId);
        List<Idoso> idosos = idosoRepository.findByInstituicaoId(instituicaoId);

        return new RelatorioInstituicaoDTO(
                LocalDateTime.now(),
                montarSecaoCuidadorInstituicao(cuidadores),
                montarSecaoIdosoInstituicao(idosos));
    }

    private SecaoCuidadorInstituicaoDTO montarSecaoCuidadorInstituicao(List<Cuidador> lista) {
        long ativos = lista.stream().filter(c -> c.getStatus() == Status.ATIVO).count();
        long inativos = lista.stream().filter(c -> c.getStatus() == Status.INATIVO).count();

        List<ItemCuidadorInstituicaoDTO> items = lista.stream()
                .map(c -> new ItemCuidadorInstituicaoDTO(
                        c.getId(),
                        TextoUtils.paraExibicao(c.getNome()),
                        c.getEmail(),
                        c.getCpf(),
                        c.getStatus().name()))
                .toList();

        return new SecaoCuidadorInstituicaoDTO(lista.size(), ativos, inativos, items);
    }

    private SecaoIdosoInstituicaoDTO montarSecaoIdosoInstituicao(List<Idoso> lista) {
        long ativos = lista.stream().filter(i -> i.getStatus() == Status.ATIVO).count();
        long inativos = lista.stream().filter(i -> i.getStatus() == Status.INATIVO).count();

        List<ItemIdosoInstituicaoDTO> items = lista.stream()
                .map(i -> new ItemIdosoInstituicaoDTO(
                        i.getId(),
                        TextoUtils.paraExibicao(i.getNome()),
                        i.getCpf(),
                        i.getObservacoes(),
                        i.getStatus().name()))
                .toList();

        return new SecaoIdosoInstituicaoDTO(lista.size(), ativos, inativos, items);
    }
}