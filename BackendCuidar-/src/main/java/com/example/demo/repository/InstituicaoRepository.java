package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Instituicao;
import com.example.demo.enums.Status;

@Repository
public interface InstituicaoRepository extends JpaRepository<Instituicao, Integer> {

    Optional<Instituicao> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    Page<Instituicao> findByStatus(Status status, Pageable pageable);

    Optional<Instituicao> findByEmail(String email);

}