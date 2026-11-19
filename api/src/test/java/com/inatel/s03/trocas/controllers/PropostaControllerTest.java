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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PropostaControllerTest {

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
    void criarPropostaRetorna201ComLocationEProposta() throws Exception {
        String troca = criarTroca("dono", null, 3);

        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta(troca, "jogador2", "c9")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/propostas/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.idTroca").value(troca))
                .andExpect(jsonPath("$.idJogador").value("jogador2"))
                .andExpect(jsonPath("$.idCartaOfertada").value("c9"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void validaCamposObrigatorios() throws Exception {
        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    void naoCriaPropostaParaTrocaInexistente() throws Exception {
        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta("nao-existe", "jogador2", "c9")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void donoNaoPodeProporParaAPropriaTroca() throws Exception {
        String troca = criarTroca("dono", null, 3);

        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta(troca, "dono", "c9")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void comCartaDesejadaSoAceitaPropostaComEssaCarta() throws Exception {
        String troca = criarTroca("dono", "c2", 3);

        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta(troca, "jogador2", "outra")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("c2")));

        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta(troca, "jogador2", "c2")))
                .andExpect(status().isCreated());
    }

    @Test
    void naoCriaPropostaSeJogadorNaoPossuiACarta() throws Exception {
        String troca = criarTroca("dono", null, 3);
        when(inventario.possuiCarta("jogador2", "c9")).thenReturn(false);

        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta(troca, "jogador2", "c9")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("não possui a carta")));
    }

    @Test
    void respeitaOLimiteDePropostasPendentes() throws Exception {
        String troca = criarTroca("dono", null, 2);
        String primeira = criarProposta(troca, "j1", "c1");
        criarProposta(troca, "j2", "c2");

        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta(troca, "j3", "c3")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("limite")));

        // cancelar uma proposta libera a vaga
        mvc.perform(post("/propostas/{id}/cancelar", primeira)).andExpect(status().isOk());
        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta(troca, "j3", "c3")))
                .andExpect(status().isCreated());
    }

    @Test
    void naoCriaPropostaParaTrocaCancelada() throws Exception {
        String troca = criarTroca("dono", null, 3);
        mvc.perform(post("/trocas/{id}/cancelar", troca)).andExpect(status().isOk());

        mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta(troca, "jogador2", "c9")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void consultaAsPropostasDeUmaTroca() throws Exception {
        String troca = criarTroca("dono", null, 5);
        String outraTroca = criarTroca("dono", null, 5);
        String p1 = criarProposta(troca, "j1", "c1");
        String p2 = criarProposta(troca, "j2", "c2");
        criarProposta(outraTroca, "j3", "c3"); // de outra troca, não deve aparecer

        mvc.perform(post("/propostas/consultar").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(troca)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(p1)) // mais antigas primeiro
                .andExpect(jsonPath("$.data[1].id").value(p2))
                .andExpect(jsonPath("$.pagination.total").value(2))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.lastPage").value(1));
    }

    @Test
    void consultaFiltraPorStatus() throws Exception {
        String troca = criarTroca("dono", null, 5);
        String pendente = criarProposta(troca, "j1", "c1");
        String cancelada = criarProposta(troca, "j2", "c2");
        mvc.perform(post("/propostas/{id}/cancelar", cancelada)).andExpect(status().isOk());

        mvc.perform(post("/propostas/consultar").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idTroca\": \"%s\", \"status\": \"PENDENTE\" }".formatted(troca)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(pendente));

        mvc.perform(post("/propostas/consultar").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idTroca\": \"%s\", \"status\": \"CANCELADA\" }".formatted(troca)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(cancelada));
    }

    @Test
    void consultaFiltraPorJogador() throws Exception {
        String troca = criarTroca("dono", null, 5);
        criarProposta(troca, "j1", "c1");
        String doJ2 = criarProposta(troca, "j2", "c2");

        mvc.perform(post("/propostas/consultar").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idTroca\": \"%s\", \"idJogador\": \"j2\" }".formatted(troca)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(doJ2))
                .andExpect(jsonPath("$.pagination.total").value(1));
    }

    @Test
    void consultaCombinaOsFiltros() throws Exception {
        String troca = criarTroca("dono", null, 5);
        String p1 = criarProposta(troca, "j1", "c1");
        criarProposta(troca, "j2", "c2");
        String p3 = criarProposta(troca, "j1", "c3");
        mvc.perform(post("/propostas/{id}/cancelar", p3)).andExpect(status().isOk());

        // só as pendentes do j1: a p1 (a p3 é do j1, mas está cancelada; a do j2 é de outro jogador)
        mvc.perform(post("/propostas/consultar").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idTroca\": \"%s\", \"status\": \"PENDENTE\", \"idJogador\": \"j1\" }"
                                .formatted(troca)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(p1));
    }

    @Test
    void consultaPagina() throws Exception {
        String troca = criarTroca("dono", null, 5);
        criarProposta(troca, "j1", "c1");
        criarProposta(troca, "j2", "c2");
        criarProposta(troca, "j3", "c3");

        mvc.perform(post("/propostas/consultar").param("perPage", "2").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(troca)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.pagination.total").value(3))
                .andExpect(jsonPath("$.pagination.lastPage").value(2));
    }

    @Test
    void consultaExigeIdTroca() throws Exception {
        mvc.perform(post("/propostas/consultar").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mvc.perform(post("/propostas/consultar").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"status\": \"PENDENTE\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void consultaRejeitaCorpoAusenteOuStatusInvalido() throws Exception {
        mvc.perform(post("/propostas/consultar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        mvc.perform(post("/propostas/consultar").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idTroca\": \"x\", \"status\": \"INEXISTENTE\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void consultaDeTrocaInexistenteRetorna404() throws Exception {
        mvc.perform(post("/propostas/consultar").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta("nao-existe")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void naoOrdenaPorCampoNaoPermitido() throws Exception {
        String troca = criarTroca("dono", null, 5);

        mvc.perform(post("/propostas/consultar").param("sort", "versao").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(troca)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void getEmPropostasNaoExisteMais() throws Exception {
        mvc.perform(get("/propostas")).andExpect(status().isMethodNotAllowed());
    }

    // ---------- Consultar: GET /trocas/{id}/propostas ----------

    @Test
    void listaAsPropostasDeUmaTrocaPelaRotaDaTroca() throws Exception {
        String troca = criarTroca("dono", null, 5);
        String p1 = criarProposta(troca, "j1", "c1");
        String p2 = criarProposta(troca, "j2", "c2");

        mvc.perform(get("/trocas/{id}/propostas", troca))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(p1))
                .andExpect(jsonPath("$.data[1].id").value(p2))
                .andExpect(jsonPath("$.pagination.total").value(2));
    }

    @Test
    void trocaSemPropostasRetornaListaVazia() throws Exception {
        String troca = criarTroca("dono", null, 5);

        mvc.perform(get("/trocas/{id}/propostas", troca))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.lastPage").value(1));
    }

    @Test
    void rotaDaTrocaDeTrocaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/trocas/{id}/propostas", "nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ---------- Buscar / aceitar / cancelar ----------

    @Test
    void buscaPropostaPorId() throws Exception {
        String troca = criarTroca("dono", null, 3);
        String id = criarProposta(troca, "j1", "c1");

        mvc.perform(get("/propostas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mvc.perform(get("/propostas/{id}", "nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void aceitarConcluiATrocaERecusaAsOutrasPropostas() throws Exception {
        String troca = criarTroca("dono", null, 5);
        String escolhida = criarProposta(troca, "j1", "c1");
        String outra = criarProposta(troca, "j2", "c2");
        String cancelada = criarProposta(troca, "j3", "c3");
        mvc.perform(post("/propostas/{id}/cancelar", cancelada)).andExpect(status().isOk());

        mvc.perform(post("/propostas/{id}/aceitar", escolhida))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITA"));

        mvc.perform(get("/propostas/{id}", outra))
                .andExpect(jsonPath("$.status").value("RECUSADA"));
        mvc.perform(get("/propostas/{id}", cancelada))
                .andExpect(jsonPath("$.status").value("CANCELADA")); // continua cancelada
        mvc.perform(get("/trocas/{id}", troca))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void naoAceitaPropostaDeTrocaJaConcluida() throws Exception {
        String troca = criarTroca("dono", null, 5);
        String escolhida = criarProposta(troca, "j1", "c1");
        String recusada = criarProposta(troca, "j2", "c2");
        mvc.perform(post("/propostas/{id}/aceitar", escolhida)).andExpect(status().isOk());

        mvc.perform(post("/propostas/{id}/aceitar", recusada))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void cancelaPropostaEImpedeCancelarDeNovo() throws Exception {
        String troca = criarTroca("dono", null, 3);
        String id = criarProposta(troca, "j1", "c1");

        mvc.perform(post("/propostas/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mvc.perform(post("/propostas/{id}/cancelar", id))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelarATrocaCancelaAsPropostasPendentes() throws Exception {
        String troca = criarTroca("dono", null, 5);
        String p1 = criarProposta(troca, "j1", "c1");
        String p2 = criarProposta(troca, "j2", "c2");

        mvc.perform(post("/trocas/{id}/cancelar", troca)).andExpect(status().isOk());

        mvc.perform(get("/propostas/{id}", p1)).andExpect(jsonPath("$.status").value("CANCELADA"));
        mvc.perform(get("/propostas/{id}", p2)).andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    // ---------- Auxiliares ----------

    private String criarTroca(String idJogador, String idCartaDesejada, int limite) throws Exception {
        String corpo = """
                { "idJogador": "%s", "idCartaOfertada": "c1", %s "limitePropostas": %d }
                """.formatted(idJogador,
                idCartaDesejada == null ? "" : "\"idCartaDesejada\": \"" + idCartaDesejada + "\",", limite);
        String resposta = mvc.perform(post("/trocas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("id").asText();
    }

    private String criarProposta(String idTroca, String idJogador, String idCarta) throws Exception {
        String resposta = mvc.perform(post("/propostas").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProposta(idTroca, idJogador, idCarta)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("id").asText();
    }

    private static String corpoProposta(String idTroca, String idJogador, String idCarta) {
        return """
                { "idTroca": "%s", "idJogador": "%s", "idCartaOfertada": "%s" }
                """.formatted(idTroca, idJogador, idCarta);
    }

    private static String corpoConsulta(String idTroca) {
        return "{ \"idTroca\": \"%s\" }".formatted(idTroca);
    }
}