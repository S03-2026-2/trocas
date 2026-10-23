package com.inatel.s03.trocas.troca;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrocaRepository extends JpaRepository<Troca, String>{

}
