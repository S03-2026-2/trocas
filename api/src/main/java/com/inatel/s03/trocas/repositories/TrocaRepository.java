package com.inatel.s03.trocas.repositories;

import com.inatel.s03.trocas.troca.Troca;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrocaRepository extends JpaRepository<Troca, String>{

}
