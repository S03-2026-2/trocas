package com.inatel.s03.trocas.repositories;

import com.inatel.s03.trocas.proposta.Proposta;
import com.inatel.s03.trocas.proposta.StatusProposta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropostaRepository extends JpaRepository<Proposta, String> {

    Page<Proposta> findByIdTroca(String idTroca, Pageable paginacao);

    List<Proposta> findByIdTrocaAndStatus(String idTroca, StatusProposta status);

    long countByIdTrocaAndStatus(String idTroca, StatusProposta status);
}