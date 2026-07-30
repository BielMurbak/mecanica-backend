package br.com.mecanica.backend.dto;

public record LoginResponse(
        String username,
        String role
) {
}
