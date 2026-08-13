package br.com.mecanica.backend.integration;

import br.com.mecanica.backend.dto.MecanicoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MecanicoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void fluxoCompletoCrudDeveFuncionarDePontaAPonta() throws Exception {
        MecanicoRequest criacao = new MecanicoRequest("Joao Silva", "Motor", "11999998888", "joao@oficina.com", true);

        String respostaCriacao = mockMvc.perform(post("/api/mecanicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(criacao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Joao Silva"))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(respostaCriacao).get("id").asLong();

        mockMvc.perform(get("/api/mecanicos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao Silva"));

        mockMvc.perform(get("/api/mecanicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        MecanicoRequest atualizacao = new MecanicoRequest("Joao Atualizado", "Suspensao", "11977776666", "joao.novo@oficina.com", false);
        mockMvc.perform(put("/api/mecanicos/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao Atualizado"))
                .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(delete("/api/mecanicos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/mecanicos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void buscarDeveRetornar404QuandoMecanicoNaoExisteNoBanco() throws Exception {
        mockMvc.perform(get("/api/mecanicos/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void criarDeveRetornar400QuandoDadosSaoInvalidos() throws Exception {
        MecanicoRequest invalido = new MecanicoRequest("", null, null, "email-invalido", true);

        mockMvc.perform(post("/api/mecanicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void listarSemAutenticacaoDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/mecanicos"))
                .andExpect(status().isUnauthorized());
    }
}
