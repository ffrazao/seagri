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

import java.util.List;
import java.util.stream.Collectors;

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

        String usuarioIdAutenticado = obterUsuarioAutenticado();

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
                usuarioIdAutenticado
        );

        return created(converterParaDto(eventoSalvo));
    }

    // NOVA ROTA: Listar o histórico do próprio usuário logado
    @GetMapping
    public ResponseEntity<ApiResponse<List<RegistroPresencaResponseDTO>>> listarHistorico() {
        
        // Garante que o usuário só pode ver o próprio histórico
        String usuarioIdAutenticado = obterUsuarioAutenticado();
        
        // Busca os registros do usuário ordenados por data (mais recentes primeiro)
        List<RegistroPresenca> historico = registroPresencaSrv.buscarHistoricoPorUsuario(usuarioIdAutenticado);
        
        List<RegistroPresencaResponseDTO> resposta = historico.stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
                
        return ok(resposta);
    }

    // Método utilitário reaproveitável
    private RegistroPresencaResponseDTO converterParaDto(RegistroPresenca entidade) {
        return RegistroPresencaResponseDTO.builder()
                .id(entidade.getId())
                .usuarioId(entidade.getUsuarioId())
                .statusTecnico(entidade.getStatusTecnico())
                .statusAdministrativo(entidade.getStatusAdministrativo())
                .recebidoNoServidorEm(entidade.getRecebidoNoServidorEm())
                .build();
    }
}