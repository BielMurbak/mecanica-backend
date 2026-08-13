package br.com.mecanica.backend.model;

import br.com.mecanica.backend.dto.MecanicoResponse;
import br.com.mecanica.backend.entity.Mecanico;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MecanicoTest {

    @Test
    void builderDevePreencherTodosOsCampos() {
        Mecanico mecanico = Mecanico.builder()
                .id(1L)
                .nome("Joao Silva")
                .especialidade("Motor")
                .telefone("11999998888")
                .email("joao@oficina.com")
                .ativo(true)
                .build();

        assertThat(mecanico.getId()).isEqualTo(1L);
        assertThat(mecanico.getNome()).isEqualTo("Joao Silva");
        assertThat(mecanico.getEspecialidade()).isEqualTo("Motor");
        assertThat(mecanico.getTelefone()).isEqualTo("11999998888");
        assertThat(mecanico.getEmail()).isEqualTo("joao@oficina.com");
        assertThat(mecanico.isAtivo()).isTrue();
    }

    @Test
    void builderDeveAplicarAtivoTrueComoPadrao() {
        Mecanico mecanico = Mecanico.builder()
                .nome("Joao Silva")
                .build();

        assertThat(mecanico.isAtivo()).isTrue();
    }

    @Test
    void settersDevemAtualizarOsCampos() {
        Mecanico mecanico = new Mecanico();

        mecanico.setId(2L);
        mecanico.setNome("Maria Souza");
        mecanico.setEspecialidade("Eletrica");
        mecanico.setTelefone("11988887777");
        mecanico.setEmail("maria@oficina.com");
        mecanico.setAtivo(false);

        assertThat(mecanico.getId()).isEqualTo(2L);
        assertThat(mecanico.getNome()).isEqualTo("Maria Souza");
        assertThat(mecanico.getEspecialidade()).isEqualTo("Eletrica");
        assertThat(mecanico.getTelefone()).isEqualTo("11988887777");
        assertThat(mecanico.getEmail()).isEqualTo("maria@oficina.com");
        assertThat(mecanico.isAtivo()).isFalse();
    }

    @Test
    void fromDeveMapearMecanicoParaMecanicoResponse() {
        Mecanico mecanico = Mecanico.builder()
                .id(3L)
                .nome("Pedro Alves")
                .especialidade("Suspensao")
                .telefone("11977776666")
                .email("pedro@oficina.com")
                .ativo(true)
                .build();

        MecanicoResponse response = MecanicoResponse.from(mecanico);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.nome()).isEqualTo("Pedro Alves");
        assertThat(response.especialidade()).isEqualTo("Suspensao");
        assertThat(response.telefone()).isEqualTo("11977776666");
        assertThat(response.email()).isEqualTo("pedro@oficina.com");
        assertThat(response.ativo()).isTrue();
    }
}
