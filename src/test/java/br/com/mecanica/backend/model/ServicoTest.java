package br.com.mecanica.backend.model;

import br.com.mecanica.backend.dto.ServicoResponse;
import br.com.mecanica.backend.entity.Servico;
import br.com.mecanica.backend.entity.StatusServico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ServicoTest {

    @Test
    void builderDevePreencherTodosOsCampos() {
        Servico servico = Servico.builder()
                .id(1L)
                .descricao("Troca de oleo")
                .valor(new BigDecimal("150.00"))
                .mecanico("Joao Silva")
                .carro("Fiat Uno")
                .duracaoEstimadaDias(1)
                .status(StatusServico.EM_ANDAMENTO)
                .build();

        assertThat(servico.getId()).isEqualTo(1L);
        assertThat(servico.getDescricao()).isEqualTo("Troca de oleo");
        assertThat(servico.getValor()).isEqualByComparingTo("150.00");
        assertThat(servico.getMecanico()).isEqualTo("Joao Silva");
        assertThat(servico.getCarro()).isEqualTo("Fiat Uno");
        assertThat(servico.getDuracaoEstimadaDias()).isEqualTo(1);
        assertThat(servico.getStatus()).isEqualTo(StatusServico.EM_ANDAMENTO);
    }

    @Test
    void builderDeveAplicarStatusPendenteComoPadrao() {
        Servico servico = Servico.builder()
                .descricao("Troca de oleo")
                .valor(BigDecimal.TEN)
                .build();

        assertThat(servico.getStatus()).isEqualTo(StatusServico.PENDENTE);
    }

    @Test
    void settersDevemAtualizarOsCampos() {
        Servico servico = new Servico();

        servico.setId(2L);
        servico.setDescricao("Alinhamento");
        servico.setValor(new BigDecimal("80.00"));
        servico.setMecanico("Maria Souza");
        servico.setCarro("Gol");
        servico.setDuracaoEstimadaDias(2);
        servico.setStatus(StatusServico.CONCLUIDO);

        assertThat(servico.getId()).isEqualTo(2L);
        assertThat(servico.getDescricao()).isEqualTo("Alinhamento");
        assertThat(servico.getValor()).isEqualByComparingTo("80.00");
        assertThat(servico.getMecanico()).isEqualTo("Maria Souza");
        assertThat(servico.getCarro()).isEqualTo("Gol");
        assertThat(servico.getDuracaoEstimadaDias()).isEqualTo(2);
        assertThat(servico.getStatus()).isEqualTo(StatusServico.CONCLUIDO);
    }

    @Test
    void fromDeveMapearServicoParaServicoResponse() {
        Servico servico = Servico.builder()
                .id(3L)
                .descricao("Revisao completa")
                .valor(new BigDecimal("300.00"))
                .mecanico("Pedro Alves")
                .carro("Onix")
                .duracaoEstimadaDias(3)
                .status(StatusServico.PENDENTE)
                .build();

        ServicoResponse response = ServicoResponse.from(servico);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.descricao()).isEqualTo("Revisao completa");
        assertThat(response.valor()).isEqualByComparingTo("300.00");
        assertThat(response.mecanico()).isEqualTo("Pedro Alves");
        assertThat(response.carro()).isEqualTo("Onix");
        assertThat(response.duracaoEstimadaDias()).isEqualTo(3);
        assertThat(response.status()).isEqualTo(StatusServico.PENDENTE);
    }
}
