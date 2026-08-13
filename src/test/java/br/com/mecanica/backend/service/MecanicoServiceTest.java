package br.com.mecanica.backend.service;

import br.com.mecanica.backend.dto.MecanicoRequest;
import br.com.mecanica.backend.dto.MecanicoResponse;
import br.com.mecanica.backend.entity.Mecanico;
import br.com.mecanica.backend.exception.ResourceNotFoundException;
import br.com.mecanica.backend.repository.MecanicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MecanicoServiceTest {

    @Mock
    private MecanicoRepository mecanicoRepository;

    private MecanicoService mecanicoService;

    @BeforeEach
    void setUp() {
        mecanicoService = new MecanicoService(mecanicoRepository);
    }

    private Mecanico mecanicoExistente() {
        return Mecanico.builder()
                .id(1L)
                .nome("Joao Silva")
                .especialidade("Motor")
                .telefone("11999998888")
                .email("joao@oficina.com")
                .ativo(true)
                .build();
    }

    @Test
    void listarDeveRetornarTodosOsMecanicosMapeados() {
        Mecanico mecanico = mecanicoExistente();
        when(mecanicoRepository.findAll()).thenReturn(List.of(mecanico));

        List<MecanicoResponse> resultado = mecanicoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(0).nome()).isEqualTo("Joao Silva");
        verify(mecanicoRepository).findAll();
    }

    @Test
    void listarDeveRetornarListaVaziaQuandoNaoHaMecanicos() {
        when(mecanicoRepository.findAll()).thenReturn(List.of());

        List<MecanicoResponse> resultado = mecanicoService.listar();

        assertThat(resultado).isEmpty();
    }

    @Test
    void buscarDeveRetornarMecanicoQuandoEncontrado() {
        when(mecanicoRepository.findById(1L)).thenReturn(Optional.of(mecanicoExistente()));

        MecanicoResponse resultado = mecanicoService.buscar(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Joao Silva");
    }

    @Test
    void buscarDeveLancarExcecaoQuandoNaoEncontrado() {
        when(mecanicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mecanicoService.buscar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void criarDevePersistirNovoMecanicoERetornarResponse() {
        MecanicoRequest request = new MecanicoRequest("Maria Souza", "Eletrica", "11988887777", "maria@oficina.com", true);
        Mecanico salvo = Mecanico.builder()
                .id(2L)
                .nome(request.nome())
                .especialidade(request.especialidade())
                .telefone(request.telefone())
                .email(request.email())
                .ativo(request.ativo())
                .build();
        when(mecanicoRepository.save(any(Mecanico.class))).thenReturn(salvo);

        MecanicoResponse resultado = mecanicoService.criar(request);

        ArgumentCaptor<Mecanico> captor = ArgumentCaptor.forClass(Mecanico.class);
        verify(mecanicoRepository).save(captor.capture());
        Mecanico enviado = captor.getValue();
        assertThat(enviado.getId()).isNull();
        assertThat(enviado.getNome()).isEqualTo("Maria Souza");
        assertThat(enviado.getEspecialidade()).isEqualTo("Eletrica");

        assertThat(resultado.id()).isEqualTo(2L);
        assertThat(resultado.nome()).isEqualTo("Maria Souza");
    }

    @Test
    void atualizarDeveAlterarCamposDoMecanicoExistente() {
        Mecanico existente = mecanicoExistente();
        MecanicoRequest request = new MecanicoRequest("Joao Atualizado", "Suspensao", "11977776666", "joao.novo@oficina.com", false);
        when(mecanicoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(mecanicoRepository.save(any(Mecanico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MecanicoResponse resultado = mecanicoService.atualizar(1L, request);

        assertThat(resultado.nome()).isEqualTo("Joao Atualizado");
        assertThat(resultado.especialidade()).isEqualTo("Suspensao");
        assertThat(resultado.telefone()).isEqualTo("11977776666");
        assertThat(resultado.email()).isEqualTo("joao.novo@oficina.com");
        assertThat(resultado.ativo()).isFalse();
        verify(mecanicoRepository).save(existente);
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoMecanicoNaoExiste() {
        MecanicoRequest request = new MecanicoRequest("X", null, null, null, true);
        when(mecanicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mecanicoService.atualizar(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(mecanicoRepository, never()).save(any());
    }

    @Test
    void removerDeveDeletarMecanicoExistente() {
        Mecanico existente = mecanicoExistente();
        when(mecanicoRepository.findById(1L)).thenReturn(Optional.of(existente));

        mecanicoService.remover(1L);

        verify(mecanicoRepository).delete(existente);
    }

    @Test
    void removerDeveLancarExcecaoQuandoMecanicoNaoExiste() {
        when(mecanicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mecanicoService.remover(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(mecanicoRepository, never()).delete(any());
    }
}
