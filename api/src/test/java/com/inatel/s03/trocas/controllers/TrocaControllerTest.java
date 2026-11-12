package com.inatel.s03.trocas.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inatel.s03.trocas.integracoes.Inventario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TrocaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockBean
    private Inventario inventario;

    @BeforeEach
    void jogadorPossuiCartas() {
        when(inventario.possuiCarta(anyString(), anyString())).thenReturn(true);
    }

    // ---------- Criar ----------

    @Test
    void criarTrocaRetorna201ComLocationEATroca() throws Exception {
        mvc.perform(post("/trocas").contentType(MediaType.APPLICATION_JSON).content(corpoTroca("jogador1")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/trocas/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.idCartaOfertada").value("c1"))
                .andExpect(jsonPath("$.idCartaDesejada").value("c2"));
    }

    @Test
    void cartaDesejadaEhOpcional() throws Exception {
        mvc.perform(post("/trocas").contentType(MediaType.APPLICATION_JSON).content("""
                        { "idJogador": "jogador1", "idCartaOfertada": "c1", "limitePropostas": 3 }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCartaDesejada").value(nullValue()));
    }

    @Test
    void naoCriaTrocaSeJogadorNaoPossuiCarta() throws Exception {
        when(inventario.possuiCarta("jogador1", "c1")).thenReturn(false);

        mvc.perform(post("/trocas").contentType(MediaType.APPLICATION_JSON).content(corpoTroca("jogador1")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.detail").value(containsString("não possui a carta")))
                .andExpect(jsonPath("$.instance").value("/trocas"));
    }

    @Test
    void validaCamposObrigatorios() throws Exception {
        mvc.perform(post("/trocas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mvc.perform(post("/trocas").contentType(MediaType.APPLICATION_JSON).content("{ isso não é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    // ---------- Buscar / cancelar ----------

    @Test
    void consultaPorId() throws Exception {
        String id = criarTroca("jogador1");

        mvc.perform(get("/trocas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void trocaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/trocas/{id}", "nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.instance").value("/trocas/nao-existe"));
    }

    @Test
    void cancelaTrocaEImpedeCancelarDeNovo() throws Exception {
        String id = criarTroca("jogador1");

        mvc.perform(post("/trocas/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mvc.perform(post("/trocas/{id}/cancelar", id))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    // ---------- Listar: filtros e paginação ----------

    @Test
    void filtraPorJogadorEStatus() throws Exception {
        String jogador = jogadorUnico();
        String cancelada = criarTroca(jogador);
        criarTroca(jogador);
        criarTroca(jogadorUnico()); // de outro jogador, não deve aparecer
        mvc.perform(post("/trocas/{id}/cancelar", cancelada));

        mvc.perform(get("/trocas").param("idJogador", jogador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.total").value(2));

        mvc.perform(get("/trocas").param("idJogador", jogador).param("status", "CANCELADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.total").value(1))
                .andExpect(jsonPath("$.data[0].id").value(cancelada));
    }

    @Test
    void paginaComecaEm1EOrdenaDoMaisRecente() throws Exception {
        String jogador = jogadorUnico();
        criarTroca(jogador);
        criarTroca(jogador);
        String maisRecente = criarTroca(jogador);

        mvc.perform(get("/trocas").param("idJogador", jogador).param("perPage", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(maisRecente))
                .andExpect(jsonPath("$.pagination.total").value(3))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.perPage").value(2))
                .andExpect(jsonPath("$.pagination.lastPage").value(2));

        mvc.perform(get("/trocas").param("idJogador", jogador).param("perPage", "2").param("page", "2"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.pagination.page").value(2));
    }

    @Test
    void listaVaziaTemPagina1() throws Exception {
        mvc.perform(get("/trocas").param("idJogador", jogadorUnico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.pagination.total").value(0))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.lastPage").value(1));
    }

    @Test
    void ordenaPorCampoPermitido() throws Exception {
        mvc.perform(get("/trocas").param("sort", "status,asc"))
                .andExpect(status().isOk());
    }

    @Test
    void naoOrdenaPorCampoInternoOuInexistente() throws Exception {
        mvc.perform(get("/trocas").param("sort", "versao"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        mvc.perform(get("/trocas").param("sort", "xyz"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statusInvalidoRetorna400() throws Exception {
        mvc.perform(get("/trocas").param("status", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0]").value(containsString("status")));
    }

    // ---------- Erros do Spring MVC também no formato RFC 9457 ----------

    @Test
    void rotaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void metodoNaoSuportadoRetorna405() throws Exception {
        mvc.perform(delete("/trocas"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value("REQUEST_ERROR"));
    }

    // ---------- Auxiliares ----------

    private String criarTroca(String idJogador) throws Exception {
        String corpo = mvc.perform(post("/trocas").contentType(MediaType.APPLICATION_JSON).content(corpoTroca(idJogador)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("id").asText();
    }

    private static String corpoTroca(String idJogador) {
        return """
                {
                  "idJogador": "%s",
                  "idCartaOfertada": "c1",
                  "idCartaDesejada": "c2",
                  "limitePropostas": 3
                }
                """.formatted(idJogador);
    }

    /** Os testes compartilham o mesmo banco; um jogador novo isola os dados de cada teste. */
    private static String jogadorUnico() {
        return "jogador-" + UUID.randomUUID();
    }
}
