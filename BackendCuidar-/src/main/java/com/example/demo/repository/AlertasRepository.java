package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Alertas;
import com.example.demo.enums.StatusAlertas;

@Repository
public interface AlertasRepository extends JpaRepository<Alertas, Integer> {

    @Query("""
            select a
            from Alertas a
            where a.idoso.id in (
                select v.idoso.id
                from Vinculo v
                where v.cuidador.id = :cuidadorId
            )
              and a.statusAlertas <> :status
            """)
    Page<Alertas> findNaoCanceladosPorCuidador(Integer cuidadorId, StatusAlertas status, Pageable pageable);

    @Query("""
            select a
            from Alertas a
            where a.idoso.id = :idosoId
              and a.idoso.id in (
                select v.idoso.id
                from Vinculo v
                where v.cuidador.id = :cuidadorId
              )
              and a.statusAlertas <> :status
            """)
    Page<Alertas> findNaoCanceladosPorIdosoECuidador(
            Integer idosoId,
            Integer cuidadorId,
            StatusAlertas status,
            Pageable pageable);

    Page<Alertas> findByIdosoIdAndStatusAlertasNot(Integer idosoId, StatusAlertas status, Pageable pageable);
}
