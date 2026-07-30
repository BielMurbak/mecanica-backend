package br.com.mecanica.backend.service;

import br.com.mecanica.backend.dto.ServicoRequest;
import br.com.mecanica.backend.dto.ServicoResponse;
import br.com.mecanica.backend.entity.Servico;
import br.com.mecanica.backend.exception.ResourceNotFoundException;
import br.com.mecanica.backend.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public List<ServicoResponse> listar() {
        return servicoRepository.findAll().stream().map(ServicoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscar(Long id) {
        return ServicoResponse.from(buscarEntidade(id));
    }

    public ServicoResponse criar(ServicoRequest request) {
        Servico servico = Servico.builder()
                .descricao(request.descricao())
                .valor(request.valor())
                .mecanico(request.mecanico())
                .carro(request.carro())
                .duracaoEstimadaDias(request.duracaoEstimadaDias())
                .status(request.status())
                .build();
        return ServicoResponse.from(servicoRepository.save(servico));
    }

    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico servico = buscarEntidade(id);
        servico.setDescricao(request.descricao());
        servico.setValor(request.valor());
        servico.setMecanico(request.mecanico());
        servico.setCarro(request.carro());
        servico.setDuracaoEstimadaDias(request.duracaoEstimadaDias());
        servico.setStatus(request.status());
        return ServicoResponse.from(servicoRepository.save(servico));
    }

    public void remover(Long id) {
        Servico servico = buscarEntidade(id);
        servicoRepository.delete(servico);
    }

    private Servico buscarEntidade(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servico nao encontrado: " + id));
    }
}
