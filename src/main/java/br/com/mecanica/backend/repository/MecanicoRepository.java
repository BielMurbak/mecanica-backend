package br.com.mecanica.backend.repository;

import br.com.mecanica.backend.entity.Mecanico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MecanicoRepository extends JpaRepository<Mecanico, Long> {
}
