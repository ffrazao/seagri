package br.gov.df.seagri.dominio_central.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controller genérico para operações CRUD em contexto Global (sem Tenant).
 * Implementa a RFC-0009.
 */
public abstract class AbstractCrudController<REQ, RES, ID> extends AbstractApiController {

    // Contratos que as subclasses (Controllers específicos) deverão implementar
    // para fazer a ponte entre os DTOs e os Serviços de Aplicação
    protected abstract RES executarCriacao(REQ request);
    protected abstract RES executarAtualizacao(ID id, REQ request);
    protected abstract RES executarBuscaPorId(ID id);
    protected abstract List<RES> executarListagem(); // No futuro pode receber um objeto Pageable
    protected abstract void executarExclusao(ID id);

    @PostMapping
    public ResponseEntity<ApiResponse<RES>> criar(@Valid @RequestBody REQ request) {
        return created(executarCriacao(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RES>> atualizar(@PathVariable ID id, @Valid @RequestBody REQ request) {
        return ok(executarAtualizacao(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RES>> buscarPorId(@PathVariable ID id) {
        return ok(executarBuscaPorId(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RES>>> listar() {
        return ok(executarListagem());
    }

    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<List<RES>>> lookup() {
        // Conforme RFC-0009: Endpoint simplificado para preencher selects e combos na UI
        return ok(executarListagem());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable ID id) {
        executarExclusao(id);
        return ok(null);
    }
}