package br.com.mecanica.backend.service;

import br.com.mecanica.backend.dto.ServicoRequest;
import br.com.mecanica.backend.dto.ServicoResponse;
import br.com.mecanica.backend.entity.Servico;
import br.com.mecanica.backend.entity.StatusServico;
import br.com.mecanica.backend.exception.ResourceNotFoundException;
import br.com.mecanica.backend.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    private ServicoService servicoService;

    @BeforeEach
    void setUp() {
        servicoService = new ServicoService(servicoRepository);
    }

    private Servico servicoExistente() {
        return Servico.builder()
                .id(1L)
                .descricao("Troca de oleo")
                .valor(new BigDecimal("150.00"))
                .mecanico("Joao Silva")
                .carro("Fiat Uno")
                .duracaoEstimadaDias(1)
                .status(StatusServico.PENDENTE)
                .build();
    }

    @Test
    void listarDeveRetornarTodosOsServicosMapeados() {
        Servico servico = servicoExistente();
        when(servicoRepository.findAll()).thenReturn(List.of(servico));

        List<ServicoResponse> resultado = servicoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(0).descricao()).isEqualTo("Troca de oleo");
        verify(servicoRepository).findAll();
    }

    @Test
    void listarDeveRetornarListaVaziaQuandoNaoHaServicos() {
        when(servicoRepository.findAll()).thenReturn(List.of());

        List<ServicoResponse> resultado = servicoService.listar();

        assertThat(resultado).isEmpty();
    }

    @Test
    void buscarDeveRetornarServicoQuandoEncontrado() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoExistente()));

        ServicoResponse resultado = servicoService.buscar(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.valor()).isEqualByComparingTo("150.00");
    }

    @Test
    void buscarDeveLancarExcecaoQuandoNaoEncontrado() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.buscar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void criarDevePersistirNovoServicoERetornarResponse() {
        ServicoRequest request = new ServicoRequest("Alinhamento", new BigDecimal("80.00"), "Maria Souza", "Gol", 2, StatusServico.PENDENTE);
        Servico salvo = Servico.builder()
                .id(5L)
                .descricao(request.descricao())
                .valor(request.valor())
                .mecanico(request.mecanico())
                .carro(request.carro())
                .duracaoEstimadaDias(request.duracaoEstimadaDias())
                .status(request.status())
                .build();
        when(servicoRepository.save(any(Servico.class))).thenReturn(salvo);

        ServicoResponse resultado = servicoService.criar(request);

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(servicoRepository).save(captor.capture());
        Servico enviado = captor.getValue();
        assertThat(enviado.getId()).isNull();
        assertThat(enviado.getDescricao()).isEqualTo("Alinhamento");
        assertThat(enviado.getValor()).isEqualByComparingTo("80.00");

        assertThat(resultado.id()).isEqualTo(5L);
        assertThat(resultado.descricao()).isEqualTo("Alinhamento");
    }

    @Test
    void atualizarDeveAlterarCamposDoServicoExistente() {
        Servico existente = servicoExistente();
        ServicoRequest request = new ServicoRequest("Revisao completa", new BigDecimal("300.00"), "Pedro", "Onix", 3, StatusServico.EM_ANDAMENTO);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicoResponse resultado = servicoService.atualizar(1L, request);

        assertThat(resultado.descricao()).isEqualTo("Revisao completa");
        assertThat(resultado.valor()).isEqualByComparingTo("300.00");
        assertThat(resultado.mecanico()).isEqualTo("Pedro");
        assertThat(resultado.carro()).isEqualTo("Onix");
        assertThat(resultado.duracaoEstimadaDias()).isEqualTo(3);
        assertThat(resultado.status()).isEqualTo(StatusServico.EM_ANDAMENTO);
        verify(servicoRepository).save(existente);
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoServicoNaoExiste() {
        ServicoRequest request = new ServicoRequest("X", BigDecimal.ZERO, null, null, null, StatusServico.PENDENTE);
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.atualizar(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(servicoRepository, never()).save(any());
    }

    @Test
    void removerDeveDeletarServicoExistente() {
        Servico existente = servicoExistente();
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(existente));

        servicoService.remover(1L);

        verify(servicoRepository).delete(existente);
    }

    @Test
    void removerDeveLancarExcecaoQuandoServicoNaoExiste() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.remover(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(servicoRepository, never()).delete(any());
    }
}
