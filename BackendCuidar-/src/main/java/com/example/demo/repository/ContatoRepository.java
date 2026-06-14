package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Contato;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Integer> {

    Optional<Contato> findByTelefone(String telefone);

    Page<Contato> findByIdosos_Id(Integer idosoId, Pageable pageable);
}
