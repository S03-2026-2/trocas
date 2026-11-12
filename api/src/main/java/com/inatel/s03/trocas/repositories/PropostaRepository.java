package com.inatel.s03.trocas.repositories;

import com.inatel.s03.trocas.proposta.Proposta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropostaRepository extends JpaRepository<Proposta, String>{
}
