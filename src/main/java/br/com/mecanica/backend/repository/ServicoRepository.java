package br.com.mecanica.backend.repository;

import br.com.mecanica.backend.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
}
