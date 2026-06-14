package com.example.demo.repository;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Remedio;
import com.example.demo.enums.Status;

@Repository
public interface RemedioRepository extends JpaRepository<Remedio, Integer> {

    Page<Remedio> findByStatus(Status status, Pageable pageable);

    Page<Remedio> findByCuidadorIdAndStatus(Integer cuidadorId, Status status, Pageable pageable);

    boolean existsByNome(String nome);

    boolean existsByNomeAndCuidadorId(String nome, Integer cuidadorId);

    Optional<Remedio> findByNome(String nome);

    Optional<Remedio> findByNomeAndCuidadorId(String nome, Integer cuidadorId);

    Optional<Remedio> findByIdAndCuidadorId(Integer id, Integer cuidadorId);

}
