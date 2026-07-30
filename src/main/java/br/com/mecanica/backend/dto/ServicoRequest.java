package br.com.mecanica.backend.dto;

import br.com.mecanica.backend.entity.StatusServico;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicoRequest(
        @NotBlank @Size(max = 255) String descricao,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal valor,
        @Size(max = 120) String mecanico,
        @Size(max = 120) String carro,
        @Positive Integer duracaoEstimadaDias,
        @NotNull StatusServico status
) {
}
