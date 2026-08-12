package br.com.mecanica.backend.controller;

import br.com.mecanica.backend.dto.MecanicoRequest;
import br.com.mecanica.backend.dto.MecanicoResponse;
import br.com.mecanica.backend.exception.GlobalExceptionHandler;
import br.com.mecanica.backend.exception.ResourceNotFoundException;
import br.com.mecanica.backend.service.MecanicoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MecanicoControllerTest {

    @Mock
    private MecanicoService mecanicoService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MecanicoController controller = new MecanicoController(mecanicoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private MecanicoResponse respostaPadrao() {
        return new MecanicoResponse(1L, "Joao Silva", "Motor", "11999998888", "joao@oficina.com", true);
    }

    @Test
    void listarDeveRetornar200ComListaDeMecanicos() throws Exception {
        when(mecanicoService.listar()).thenReturn(List.of(respostaPadrao()));

        mockMvc.perform(get("/api/mecanicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Joao Silva"));
    }

    @Test
    void buscarDeveRetornar200QuandoMecanicoExiste() throws Exception {
        when(mecanicoService.buscar(1L)).thenReturn(respostaPadrao());

        mockMvc.perform(get("/api/mecanicos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao Silva"));
    }

    @Test
    void buscarDeveRetornar404QuandoMecanicoNaoExiste() throws Exception {
        when(mecanicoService.buscar(99L)).thenThrow(new ResourceNotFoundException("Mecanico nao encontrado: 99"));

        mockMvc.perform(get("/api/mecanicos/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void criarDeveRetornar201ComMecanicoCriado() throws Exception {
        MecanicoRequest request = new MecanicoRequest("Joao Silva", "Motor", "11999998888", "joao@oficina.com", true);
        when(mecanicoService.criar(any(MecanicoRequest.class))).thenReturn(respostaPadrao());

        mockMvc.perform(post("/api/mecanicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(mecanicoService).criar(any(MecanicoRequest.class));
    }

    @Test
    void criarDeveRetornar400QuandoNomeEstaEmBranco() throws Exception {
        MecanicoRequest request = new MecanicoRequest("", "Motor", "11999998888", "joao@oficina.com", true);

        mockMvc.perform(post("/api/mecanicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(mecanicoService, never()).criar(any());
    }

    @Test
    void atualizarDeveRetornar200ComMecanicoAtualizado() throws Exception {
        MecanicoRequest request = new MecanicoRequest("Joao Atualizado", "Motor", "11999998888", "joao@oficina.com", true);
        MecanicoResponse response = new MecanicoResponse(1L, "Joao Atualizado", "Motor", "11999998888", "joao@oficina.com", true);
        when(mecanicoService.atualizar(eq(1L), any(MecanicoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/mecanicos/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao Atualizado"));
    }

    @Test
    void removerDeveRetornar204QuandoMecanicoRemovido() throws Exception {
        doNothing().when(mecanicoService).remover(1L);

        mockMvc.perform(delete("/api/mecanicos/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(mecanicoService).remover(1L);
    }

    @Test
    void removerDeveRetornar404QuandoMecanicoNaoExiste() throws Exception {
        doThrow(new ResourceNotFoundException("Mecanico nao encontrado: 99")).when(mecanicoService).remover(99L);

        mockMvc.perform(delete("/api/mecanicos/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
