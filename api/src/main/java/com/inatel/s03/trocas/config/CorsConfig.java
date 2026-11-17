package com.inatel.s03.trocas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Libera chamadas vindas do navegador (CORS) apenas para os front-ends configurados
 * em app.cors.allowed-origins (variável de ambiente CORS_ALLOWED_ORIGINS).
 *
 * Sem origens configuradas, nenhuma é liberada: a API continua acessível por
 * servidores, curl e Postman, mas não por páginas web de outros domínios.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final List<String> origensPermitidas;

    public CorsConfig(@Value("${app.cors.allowed-origins:}") List<String> origensPermitidas) {
        this.origensPermitidas = origensPermitidas.stream()
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (origensPermitidas.isEmpty()) {
            return;
        }
        registry.addMapping("/**")
                .allowedOrigins(origensPermitidas.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .exposedHeaders("Location");
    }
}
