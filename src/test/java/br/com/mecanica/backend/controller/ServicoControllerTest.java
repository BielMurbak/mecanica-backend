package br.com.mecanica.backend.controller;

import br.com.mecanica.backend.dto.ServicoRequest;
import br.com.mecanica.backend.dto.ServicoResponse;
import br.com.mecanica.backend.entity.StatusServico;
import br.com.mecanica.backend.exception.GlobalExceptionHandler;
import br.com.mecanica.backend.exception.ResourceNotFoundException;
import br.com.mecanica.backend.service.ServicoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    @Mock
    private ServicoService servicoService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ServicoController controller = new ServicoController(servicoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ServicoResponse respostaPadrao() {
        return new ServicoResponse(1L, "Troca de oleo", new BigDecimal("150.00"), "Joao Silva", "Fiat Uno", 1, StatusServico.PENDENTE);
    }

    @Test
    void listarDeveRetornar200ComListaDeServicos() throws Exception {
        when(servicoService.listar()).thenReturn(List.of(respostaPadrao()));

        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].descricao").value("Troca de oleo"));
    }

    @Test
    void buscarDeveRetornar200QuandoServicoExiste() throws Exception {
        when(servicoService.buscar(1L)).thenReturn(respostaPadrao());

        mockMvc.perform(get("/api/servicos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Troca de oleo"));
    }

    @Test
    void buscarDeveRetornar404QuandoServicoNaoExiste() throws Exception {
        when(servicoService.buscar(99L)).thenThrow(new ResourceNotFoundException("Servico nao encontrado: 99"));

        mockMvc.perform(get("/api/servicos/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void criarDeveRetornar201ComServicoCriado() throws Exception {
        ServicoRequest request = new ServicoRequest("Troca de oleo", new BigDecimal("150.00"), "Joao Silva", "Fiat Uno", 1, StatusServico.PENDENTE);
        when(servicoService.criar(any(ServicoRequest.class))).thenReturn(respostaPadrao());

        mockMvc.perform(post("/api/servicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(servicoService).criar(any(ServicoRequest.class));
    }

    @Test
    void criarDeveRetornar400QuandoDescricaoEstaEmBranco() throws Exception {
        ServicoRequest request = new ServicoRequest("", new BigDecimal("150.00"), "Joao Silva", "Fiat Uno", 1, StatusServico.PENDENTE);

        mockMvc.perform(post("/api/servicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(servicoService, never()).criar(any());
    }

    @Test
    void criarDeveRetornar400QuandoValorEhNegativo() throws Exception {
        ServicoRequest request = new ServicoRequest("Troca de oleo", new BigDecimal("-10.00"), "Joao Silva", "Fiat Uno", 1, StatusServico.PENDENTE);

        mockMvc.perform(post("/api/servicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(servicoService, never()).criar(any());
    }

    @Test
    void atualizarDeveRetornar200ComServicoAtualizado() throws Exception {
        ServicoRequest request = new ServicoRequest("Revisao completa", new BigDecimal("300.00"), "Pedro", "Onix", 3, StatusServico.EM_ANDAMENTO);
        ServicoResponse response = new ServicoResponse(1L, "Revisao completa", new BigDecimal("300.00"), "Pedro", "Onix", 3, StatusServico.EM_ANDAMENTO);
        when(servicoService.atualizar(eq(1L), any(ServicoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/servicos/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Revisao completa"));
    }

    @Test
    void removerDeveRetornar204QuandoServicoRemovido() throws Exception {
        doNothing().when(servicoService).remover(1L);

        mockMvc.perform(delete("/api/servicos/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(servicoService).remover(1L);
    }

    @Test
    void removerDeveRetornar404QuandoServicoNaoExiste() throws Exception {
        doThrow(new ResourceNotFoundException("Servico nao encontrado: 99")).when(servicoService).remover(99L);

        mockMvc.perform(delete("/api/servicos/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
