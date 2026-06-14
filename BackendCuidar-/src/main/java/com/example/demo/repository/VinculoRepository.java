package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Vinculo;
import com.example.demo.enums.TipoVinculo;

@Repository
public interface VinculoRepository extends JpaRepository<Vinculo, Integer> {

    Page<Vinculo> findByIdosoId(Integer idosoId, Pageable pageable);

    Page<Vinculo> findByCuidadorId(Integer cuidadorId, Pageable pageable);

    boolean existsByIdosoIdAndCuidadorId(Integer idosoId, Integer cuidadorId);

    Optional<Vinculo> findByIdosoIdAndTipoVinculo(Integer idosoId, TipoVinculo tipoVinculo);

    boolean existsByIdosoIdAndTipoVinculo(Integer idosoId, TipoVinculo tipoVinculo);

    boolean existsByIdosoId(Integer idosoId);

    Optional<Vinculo> findFirstByIdosoIdAndIdNot(Integer idosoId, Integer id);
}