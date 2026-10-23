package com.inatel.s03.trocas.troca;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity(name = "Troca")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Troca {
    @Id
    private String id;
    private String idJogador;
    //to do: status ENUM
    private int limitePropostas;
    //to do: cartaOfertada
    //to do: cartaDesejada
    //to do propostas (list propostas)

    public Troca(String id){
        this.id = id;
    }
}
