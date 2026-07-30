package br.com.mecanica.backend.dto;

import br.com.mecanica.backend.entity.Servico;
import br.com.mecanica.backend.entity.StatusServico;

import java.math.BigDecimal;

public record ServicoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        String mecanico,
        String carro,
        Integer duracaoEstimadaDias,
        StatusServico status
) {
    public static ServicoResponse from(Servico s) {
        return new ServicoResponse(s.getId(), s.getDescricao(), s.getValor(), s.getMecanico(), s.getCarro(), s.getDuracaoEstimadaDias(), s.getStatus());
    }
}
