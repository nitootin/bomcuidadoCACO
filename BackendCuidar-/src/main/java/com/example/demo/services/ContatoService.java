package com.example.demo.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.ContatoDTO;
import com.example.demo.entity.Contato;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Idoso;
import com.example.demo.exceptions.ResourceInUseException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mappers.ContatoMapper;
import com.example.demo.repository.ContatoRepository;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;

@Service
@Transactional
public class ContatoService {

    private final ContatoRepository contatoRepository;
    private final CuidadorRepository cuidadorRepository;
    private final IdosoRepository idosoRepository;

    public ContatoService(
            ContatoRepository contatoRepository,
            CuidadorRepository cuidadorRepository,
            IdosoRepository idosoRepository) {
        this.contatoRepository = contatoRepository;
        this.cuidadorRepository = cuidadorRepository;
        this.idosoRepository = idosoRepository;
    }

    public Page<ContatoDTO> listarTodos(Pageable pageable) {
        return contatoRepository.findAll(pageable).map(ContatoMapper::toDTO);
    }

    public Page<ContatoDTO> listarPorIdoso(Integer idosoId, Pageable pageable) {
        return contatoRepository.findByIdosos_Id(idosoId, pageable).map(ContatoMapper::toDTO);
    }

    public ContatoDTO buscarPorId(Integer id) {
        Contato contato = contatoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contato", id.longValue()));

        return ContatoMapper.toDTO(contato);
    }

    public ContatoDTO criar(ContatoDTO dto) {
        Cuidador cuidador = buscarCuidador(dto.getCuidadorId());
        List<Idoso> idosos = buscarIdosos(dto.getIdosos());

        Contato contato = ContatoMapper.toEntity(dto, cuidador, idosos);
        Contato salvo = contatoRepository.save(contato);
        vincularContatoAIdosos(salvo, idosos);

        return ContatoMapper.toDTO(salvo);
    }

    public ContatoDTO atualizar(Integer id, ContatoDTO dto) {
        Contato contato = contatoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contato", id.longValue()));

        Cuidador cuidador = dto.getCuidadorId() != null
                ? buscarCuidador(dto.getCuidadorId())
                : contato.getCuidador();
        List<Idoso> idosos = dto.getIdosos() != null
                ? buscarIdosos(dto.getIdosos())
                : contato.getIdosos();

        ContatoMapper.atualizarContato(contato, dto, cuidador, idosos);
        Contato atualizado = contatoRepository.save(contato);
        vincularContatoAIdosos(atualizado, idosos);

        return ContatoMapper.toDTO(atualizado);
    }

    public void deletar(Integer id) {
        Contato contato = contatoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contato", id.longValue()));

        if (contato.getCuidador() != null || (contato.getIdosos() != null && !contato.getIdosos().isEmpty())) {
            throw new ResourceInUseException("Contato vinculado a usuario nao pode ser deletado");
        }

        contatoRepository.delete(contato);
    }

    private Cuidador buscarCuidador(Integer cuidadorId) {
        if (cuidadorId == null) return null;

        return cuidadorRepository.findById(cuidadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador", cuidadorId.longValue()));
    }

    private List<Idoso> buscarIdosos(List<Integer> idososIds) {
        if (idososIds == null || idososIds.isEmpty()) return List.of();

        List<Idoso> idosos = idosoRepository.findAllById(idososIds);

        if (idosos.size() != idososIds.size()) {
            throw new ResourceNotFoundException("Um ou mais idosos informados não foram encontrados");
        }

        return idosos;
    }

    private void vincularContatoAIdosos(Contato contato, List<Idoso> idosos) {
        if (idosos == null || idosos.isEmpty()) return;

        for (Idoso idoso : idosos) idoso.setContato(contato);
        idosoRepository.saveAll(idosos);
    }
}
