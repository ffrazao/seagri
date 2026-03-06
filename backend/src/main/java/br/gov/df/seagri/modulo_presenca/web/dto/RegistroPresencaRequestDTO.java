package br.gov.df.seagri.modulo_presenca.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RegistroPresencaRequestDTO {
    
    @NotNull(message = "O ID da organização é obrigatório")
    private UUID organizacaoId;
    
    @NotNull(message = "O ID da unidade é obrigatório")
    private UUID unidadeId;
    
    @NotNull(message = "A latitude é obrigatória")
    private Double latitude;
    
    @NotNull(message = "A longitude é obrigatória")
    private Double longitude;
    
    @NotNull(message = "A precisão do GPS é obrigatória")
    private Double precisaoGps;
    
    @NotBlank(message = "O ID do dispositivo é obrigatório")
    private String dispositivoId;
    
    @NotBlank(message = "O modo de registro é obrigatório")
    private String modoRegistro; // SELF ou SHARED_DEVICE
    
    @NotNull(message = "A data/hora de captura no dispositivo é obrigatória")
    private LocalDateTime capturadoEm;
}