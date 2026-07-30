package br.com.mecanica.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MecanicoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 80) String especialidade,
        @Size(max = 20) String telefone,
        @Email @Size(max = 120) String email,
        boolean ativo
) {
}
