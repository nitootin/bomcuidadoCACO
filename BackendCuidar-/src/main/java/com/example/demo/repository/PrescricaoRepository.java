package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Prescricao;
import com.example.demo.enums.Status;

@Repository
public interface PrescricaoRepository extends JpaRepository<Prescricao, Integer> {

    List<Prescricao> findByRemedioIdAndStatus(Integer remedioId, Status status);

    @Query("""
            select p
            from Prescricao p
            where p.status = :status
              and (p.data_fim is null or p.data_fim >= :agora)
            """)
    Page<Prescricao> findAtivasNaoVencidas(Status status, LocalDateTime agora, Pageable pageable);

    @Query("""
            select p
            from Prescricao p
            where p.idoso.id = :idosoId
              and p.status = :status
              and (p.data_fim is null or p.data_fim >= :agora)
            """)
    Page<Prescricao> findAtivasNaoVencidasPorIdoso(Integer idosoId, Status status, LocalDateTime agora, Pageable pageable);
}
