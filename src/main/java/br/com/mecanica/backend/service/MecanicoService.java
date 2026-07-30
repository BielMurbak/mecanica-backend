package br.com.mecanica.backend.service;

import br.com.mecanica.backend.dto.MecanicoRequest;
import br.com.mecanica.backend.dto.MecanicoResponse;
import br.com.mecanica.backend.entity.Mecanico;
import br.com.mecanica.backend.exception.ResourceNotFoundException;
import br.com.mecanica.backend.repository.MecanicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MecanicoService {

    private final MecanicoRepository mecanicoRepository;

    @Transactional(readOnly = true)
    public List<MecanicoResponse> listar() {
        return mecanicoRepository.findAll().stream().map(MecanicoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MecanicoResponse buscar(Long id) {
        return MecanicoResponse.from(buscarEntidade(id));
    }

    public MecanicoResponse criar(MecanicoRequest request) {
        Mecanico mecanico = Mecanico.builder()
                .nome(request.nome())
                .especialidade(request.especialidade())
                .telefone(request.telefone())
                .email(request.email())
                .ativo(request.ativo())
                .build();
        return MecanicoResponse.from(mecanicoRepository.save(mecanico));
    }

    public MecanicoResponse atualizar(Long id, MecanicoRequest request) {
        Mecanico mecanico = buscarEntidade(id);
        mecanico.setNome(request.nome());
        mecanico.setEspecialidade(request.especialidade());
        mecanico.setTelefone(request.telefone());
        mecanico.setEmail(request.email());
        mecanico.setAtivo(request.ativo());
        return MecanicoResponse.from(mecanicoRepository.save(mecanico));
    }

    public void remover(Long id) {
        Mecanico mecanico = buscarEntidade(id);
        mecanicoRepository.delete(mecanico);
    }

    private Mecanico buscarEntidade(Long id) {
        return mecanicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mecanico nao encontrado: " + id));
    }
}
