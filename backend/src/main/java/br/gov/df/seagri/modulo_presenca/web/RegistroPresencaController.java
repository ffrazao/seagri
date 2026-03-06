package br.gov.df.seagri.modulo_presenca.web;

import br.gov.df.seagri.dominio_central.web.AbstractApiController;
import br.gov.df.seagri.dominio_central.web.ApiResponse;
import br.gov.df.seagri.modulo_presenca.aplicacao.RegistroPresencaSrv;
import br.gov.df.seagri.modulo_presenca.dominio.RegistroPresenca;
import br.gov.df.seagri.modulo_presenca.web.dto.RegistroPresencaRequestDTO;
import br.gov.df.seagri.modulo_presenca.web.dto.RegistroPresencaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/presencas")
public class RegistroPresencaController extends AbstractApiController {

    private final RegistroPresencaSrv registroPresencaSrv;

    public RegistroPresencaController(RegistroPresencaSrv registroPresencaSrv) {
        this.registroPresencaSrv = registroPresencaSrv;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegistroPresencaResponseDTO>> registrar(
            @Valid @RequestBody RegistroPresencaRequestDTO dto) {

        // 1. Extrai o ID do usuário (Keycloak sub) de forma segura do token JWT
        String usuarioIdAutenticado = obterUsuarioAutenticado();

        // 2. Repassa o comando único para a camada de aplicação
        RegistroPresenca eventoSalvo = registroPresencaSrv.registrar(
                dto.getOrganizacaoId(),
                dto.getUnidadeId(),
                usuarioIdAutenticado,
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getPrecisaoGps(),
                dto.getDispositivoId(),
                dto.getModoRegistro(),
                dto.getCapturadoEm(),
                usuarioIdAutenticado // O criador é o próprio usuário logado
        );

        // 3. Converte a Entidade para DTO de Resposta (Omitindo dados sensíveis como biometria ou raw GPS)
        RegistroPresencaResponseDTO responseDTO = RegistroPresencaResponseDTO.builder()
                .id(eventoSalvo.getId())
                .usuarioId(eventoSalvo.getUsuarioId())
                .statusTecnico(eventoSalvo.getStatusTecnico())
                .statusAdministrativo(eventoSalvo.getStatusAdministrativo())
                .recebidoNoServidorEm(eventoSalvo.getRecebidoNoServidorEm())
                .build();

        // 4. Retorna envelopado no padrão ApiResponse (HTTP 201 Created)
        return created(responseDTO);
    }
}