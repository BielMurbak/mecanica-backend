package br.com.mecanica.backend.dto;

import br.com.mecanica.backend.entity.Mecanico;

public record MecanicoResponse(
        Long id,
        String nome,
        String especialidade,
        String telefone,
        String email,
        boolean ativo
) {
    public static MecanicoResponse from(Mecanico m) {
        return new MecanicoResponse(m.getId(), m.getNome(), m.getEspecialidade(), m.getTelefone(), m.getEmail(), m.isAtivo());
    }
}
