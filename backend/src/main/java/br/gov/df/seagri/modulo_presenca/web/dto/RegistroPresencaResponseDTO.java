package br.gov.df.seagri.modulo_presenca.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RegistroPresencaResponseDTO {
    private UUID id;
    private UUID organizacaoId;
    private UUID unidadeId;
    private String usuarioId;
    
    // NOVO: Adicionado para avisar o React se o registro tem ou não uma foto!
    private UUID referenciaBiometrica;
    
    // Dados do evento bruto que foram salvos
    private Double latitude;
    private Double longitude;
    private Double precisaoGps;
    private String dispositivoId;
    private String modoRegistro;
    
    // Resultados da Validação (Antifraude / RFC-0003)
    private Double pontuacaoRisco;
    private String indicadoresRisco;
    
    // Status oficiais de auditoria (RFC-008)
    private String statusTecnico;         // Ex: "RECEBIDO"
    private String statusAdministrativo;  // Ex: "VALIDO" ou "PENDENTE"
    
    // Timestamps
    private LocalDateTime capturadoEm;
    private LocalDateTime recebidoNoServidorEm;
}