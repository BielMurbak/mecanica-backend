package br.com.mecanica.backend.controller;

import br.com.mecanica.backend.dto.MecanicoRequest;
import br.com.mecanica.backend.dto.MecanicoResponse;
import br.com.mecanica.backend.service.MecanicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mecanicos")
@RequiredArgsConstructor
public class MecanicoController {

    private final MecanicoService mecanicoService;

    @GetMapping
    public List<MecanicoResponse> listar() {
        return mecanicoService.listar();
    }

    @GetMapping("/{id}")
    public MecanicoResponse buscar(@PathVariable Long id) {
        return mecanicoService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MecanicoResponse criar(@Valid @RequestBody MecanicoRequest request) {
        return mecanicoService.criar(request);
    }

    @PutMapping("/{id}")
    public MecanicoResponse atualizar(@PathVariable Long id, @Valid @RequestBody MecanicoRequest request) {
        return mecanicoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        mecanicoService.remover(id);
    }
}
