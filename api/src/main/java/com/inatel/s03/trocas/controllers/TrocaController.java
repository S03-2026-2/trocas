package com.inatel.s03.trocas.controllers;

import com.inatel.s03.trocas.troca.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/troca")
public class TrocaController {
    @Autowired
    private TrocaRepository repository;

    @PostMapping
    @Transactional
    public ResponseEntity create(@RequestBody String id) {
        try {
            Troca troca = new Troca(id);
            repository.save(troca);
            return ResponseEntity.status(HttpStatus.CREATED).body(troca);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Ocorreu um erro inesperado: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Troca>> get() {
        try {
            List<Troca> trocas = repository.findAll();
            if (trocas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.emptyList());
            }
            return ResponseEntity.ok(trocas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((List<Troca>) Map.of("erro", "Erro ao buscar trocas", "detalhes", e.getMessage()));
        }
    }
}
