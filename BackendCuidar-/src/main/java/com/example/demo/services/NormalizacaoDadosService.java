package com.example.demo.services;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Administrador;
import com.example.demo.entity.Contato;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Instituicao;
import com.example.demo.entity.Prescricao;
import com.example.demo.entity.Remedio;
import com.example.demo.repository.AdministradorRepository;
import com.example.demo.repository.ContatoRepository;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.InstituicaoRepository;
import com.example.demo.repository.PrescricaoRepository;
import com.example.demo.repository.RemedioRepository;
import com.example.demo.utils.TextoUtils;

@Service
@ConditionalOnProperty(name = "cuidar.normalizacao-dados.enabled", havingValue = "true", matchIfMissing = true)
public class NormalizacaoDadosService {

    private final AdministradorRepository administradorRepository;
    private final ContatoRepository contatoRepository;
    private final CuidadorRepository cuidadorRepository;
    private final IdosoRepository idosoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final RemedioRepository remedioRepository;

    public NormalizacaoDadosService(
            AdministradorRepository administradorRepository,
            ContatoRepository contatoRepository,
            CuidadorRepository cuidadorRepository,
            IdosoRepository idosoRepository,
            InstituicaoRepository instituicaoRepository,
            PrescricaoRepository prescricaoRepository,
            RemedioRepository remedioRepository) {
        this.administradorRepository = administradorRepository;
        this.contatoRepository = contatoRepository;
        this.cuidadorRepository = cuidadorRepository;
        this.idosoRepository = idosoRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.remedioRepository = remedioRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void normalizarDadosExistentes() {
        normalizarAdministradores();
        normalizarContatos();
        normalizarCuidadores();
        normalizarIdosos();
        normalizarInstituicoes();
        normalizarPrescricoes();
        normalizarRemedios();
    }

    private void normalizarAdministradores() {
        for (Administrador administrador : administradorRepository.findAll()) {
            administrador.setNome(TextoUtils.paraBanco(administrador.getNome()));
            administrador.setCpf(TextoUtils.limparDocumento(administrador.getCpf()));
        }
    }

    private void normalizarContatos() {
        for (Contato contato : contatoRepository.findAll()) {
            contato.setDdd(TextoUtils.limparNumero(contato.getDdd()));
            contato.setTelefone(TextoUtils.limparNumero(contato.getTelefone()));
        }
    }

    private void normalizarCuidadores() {
        for (Cuidador cuidador : cuidadorRepository.findAll()) {
            cuidador.setNome(TextoUtils.paraBanco(cuidador.getNome()));
            cuidador.setCpf(TextoUtils.limparDocumento(cuidador.getCpf()));
        }
    }

    private void normalizarIdosos() {
        for (Idoso idoso : idosoRepository.findAll()) {
            idoso.setNome(TextoUtils.paraBanco(idoso.getNome()));
            idoso.setCpf(TextoUtils.limparDocumento(idoso.getCpf()));
        }
    }

    private void normalizarInstituicoes() {
        for (Instituicao instituicao : instituicaoRepository.findAll()) {
            instituicao.setNome(TextoUtils.paraBanco(instituicao.getNome()));
            instituicao.setCnpj(TextoUtils.limparDocumento(instituicao.getCnpj()));
            instituicao.setBairro(TextoUtils.paraBanco(instituicao.getBairro()));
            instituicao.setUf(TextoUtils.paraBanco(instituicao.getUf()));
            instituicao.setCep(TextoUtils.limparDocumento(instituicao.getCep()));
        }
    }

    private void normalizarPrescricoes() {
        for (Prescricao prescricao : prescricaoRepository.findAll()) {
            prescricao.setDosagem(TextoUtils.paraBanco(prescricao.getDosagem()));
        }
    }

    private void normalizarRemedios() {
        for (Remedio remedio : remedioRepository.findAll()) {
            remedio.setNome(TextoUtils.paraBanco(remedio.getNome()));
        }
    }
}
