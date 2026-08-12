package br.com.mecanica.backend.integration;

import br.com.mecanica.backend.dto.ServicoRequest;
import br.com.mecanica.backend.entity.StatusServico;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ServicoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void fluxoCompletoCrudDeveFuncionarDePontaAPonta() throws Exception {
        ServicoRequest criacao = new ServicoRequest("Troca de oleo", new BigDecimal("150.00"), "Joao Silva", "Fiat Uno", 1, StatusServico.PENDENTE);

        String respostaCriacao = mockMvc.perform(post("/api/servicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(criacao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Troca de oleo"))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(respostaCriacao).get("id").asLong();

        mockMvc.perform(get("/api/servicos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(150.00));

        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        ServicoRequest atualizacao = new ServicoRequest("Revisao completa", new BigDecimal("300.00"), "Pedro", "Onix", 3, StatusServico.EM_ANDAMENTO);
        mockMvc.perform(put("/api/servicos/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Revisao completa"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));

        mockMvc.perform(delete("/api/servicos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/servicos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void buscarDeveRetornar404QuandoServicoNaoExisteNoBanco() throws Exception {
        mockMvc.perform(get("/api/servicos/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void criarDeveRetornar400QuandoValorEhNegativo() throws Exception {
        ServicoRequest invalido = new ServicoRequest("Troca de oleo", new BigDecimal("-10.00"), "Joao Silva", "Fiat Uno", 1, StatusServico.PENDENTE);

        mockMvc.perform(post("/api/servicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.valor").exists());
    }

    @Test
    void listarSemAutenticacaoDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isUnauthorized());
    }
}
