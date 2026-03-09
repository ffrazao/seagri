package br.gov.df.seagri.modulo_organizacao.web.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class VinculoUsuarioResponseDTO {
    private UUID id;
    private UUID organizacaoId;
    private String usuarioId;
    private String papel;
    private String status;
    private LocalDateTime criadoEm;
}