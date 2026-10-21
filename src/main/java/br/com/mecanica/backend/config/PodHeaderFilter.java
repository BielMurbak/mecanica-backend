package br.com.mecanica.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Devolve o nome do Pod/host que atendeu a requisição (X-Backend-Pod).
 * Serve para demonstrar o balanceamento entre réplicas no Kubernetes.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PodHeaderFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Backend-Pod";

    private final String podName = System.getenv().getOrDefault("HOSTNAME", "local");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        response.setHeader(HEADER, podName);
        chain.doFilter(request, response);
    }
}
