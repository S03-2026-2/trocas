package com.inatel.s03.trocas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação: o main() sobe o Spring Boot (servidor web, banco, beans).
 * Fica no pacote raiz para que o Spring encontre todas as classes dos subpacotes
 * (controllers, services, repositories...). Não precisa ser alterado no dia a dia.
 */
@SpringBootApplication
public class TrocasApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrocasApplication.class, args);
    }
}
