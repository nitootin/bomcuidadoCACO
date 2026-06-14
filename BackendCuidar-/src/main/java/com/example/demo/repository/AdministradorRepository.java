package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Administrador;
import com.example.demo.enums.Status;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {

    Optional<Administrador> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    Page<Administrador> findByStatus(Status status, Pageable pageable);
}
