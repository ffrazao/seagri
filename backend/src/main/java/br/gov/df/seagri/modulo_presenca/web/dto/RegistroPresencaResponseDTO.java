package br.gov.df.seagri.modulo_presenca.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RegistroPresencaResponseDTO {
    private UUID id;
    private String usuarioId;
    private String statusTecnico;
    private String statusAdministrativo;
    private LocalDateTime recebidoNoServidorEm;
}