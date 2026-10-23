package com.inatel.s03.trocas.proposta;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity(name = "Proposta")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Proposta {
    @Id
    private String id;
    private String idJogador;
    //to do: status
    //to do: cartaOfertada

    public Proposta(String id){
        this.id = id;
    }
    
}
