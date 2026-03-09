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

        RegistroPresenca presencaSalva = registroPresencaSrv.registrar(
                organizacaoId,
                request.getUnidadeId(),
                usuarioId,
                request.getLatitude(),
                request.getLongitude(),
                request.getPrecisaoGps(),
                request.getDispositivoId(),
                request.getModoRegistro(),
                request.getCapturadoEm(),
                usuarioId 
        );

        // Converte a Entidade salva para DTO antes de devolver ao cliente
        return created(mapper.paraDto(presencaSalva));
    }
}