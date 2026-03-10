package br.gov.df.seagri.modulo_presenca.web;

import br.gov.df.seagri.dominio_central.web.AbstractApiController;
import br.gov.df.seagri.dominio_central.web.ApiResponse;
import br.gov.df.seagri.modulo_presenca.aplicacao.RegistroPresencaSrv;
import br.gov.df.seagri.modulo_presenca.dominio.RegistroPresenca;
import br.gov.df.seagri.modulo_presenca.web.dto.RegistroPresencaMapper;
import br.gov.df.seagri.modulo_presenca.web.dto.RegistroPresencaRequestDTO;
import br.gov.df.seagri.modulo_presenca.web.dto.RegistroPresencaResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orgs/{organizacaoId}/presencas")
public class RegistroPresencaController extends AbstractApiController {

    private final RegistroPresencaSrv registroPresencaSrv;
    private final RegistroPresencaMapper mapper; // Injeção do Mapper!

    public RegistroPresencaController(RegistroPresencaSrv registroPresencaSrv, RegistroPresencaMapper mapper) {
        this.registroPresencaSrv = registroPresencaSrv;
        this.mapper = mapper;
    }

    @PostMapping
    // O Retorno agora protege a entidade e devolve o ResponseDTO
    public ResponseEntity<ApiResponse<RegistroPresencaResponseDTO>> registrar(
            @PathVariable UUID organizacaoId,
            @Valid @RequestBody RegistroPresencaRequestDTO request) {

        String usuarioId = obterUsuarioAutenticado(); 

        // CORREÇÃO 1: Chamando o novo serviço passando o DTO empacotado em vez de 10 parâmetros soltos
        RegistroPresenca presencaSalva = registroPresencaSrv.registrar(
                organizacaoId,
                usuarioId,
                request
        );

        // Converte a Entidade salva para DTO antes de devolver ao cliente
        return created(mapper.paraDto(presencaSalva));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RegistroPresencaResponseDTO>>> listarHistorico(
            @PathVariable UUID organizacaoId) {
        
        // Extrai a identidade de quem está fazendo a requisição
        String usuarioId = obterUsuarioAutenticado();

        // CORREÇÃO 2: Chamando o método com o nome correto alinhado com o seu DAO e Serviço
        List<RegistroPresenca> historico = registroPresencaSrv.buscarPorUsuario(usuarioId);

        // Converte a lista de Entidades para a lista de DTOs usando o MapStruct
        List<RegistroPresencaResponseDTO> historicoDto = historico.stream()
                .map(mapper::paraDto)
                .toList();

        return ok(historicoDto);
    }

    // Rota GET exclusiva para retornar o arquivo de imagem
    @GetMapping(value = "/{presencaId}/foto", produces = org.springframework.http.MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> exibirFoto(
            @PathVariable UUID organizacaoId,
            @PathVariable UUID presencaId) {

        String usuarioId = obterUsuarioAutenticado();
        byte[] imagem = registroPresencaSrv.buscarFotoBiometrica(organizacaoId, usuarioId, presencaId);

        if (imagem == null) {
            return ResponseEntity.notFound().build();
        }

        // Retorna os bytes da imagem com cache no navegador
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(imagem);
    }

}