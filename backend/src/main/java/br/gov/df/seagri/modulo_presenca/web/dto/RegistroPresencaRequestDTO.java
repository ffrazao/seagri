package br.gov.df.seagri.modulo_presenca.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RegistroPresencaRequestDTO {
    
    @NotNull(message = "A Unidade é obrigatória")
    private UUID unidadeId;
    
    @NotNull(message = "A Latitude é obrigatória")
    private Double latitude;
    
    @NotNull(message = "A Longitude é obrigatória")
    private Double longitude;
    
    @NotNull(message = "A Precisão do GPS é obrigatória")
    private Double precisaoGps;
    
    @NotBlank(message = "O ID do Dispositivo é obrigatório")
    private String dispositivoId;
    
    @NotBlank(message = "O Modo de Registro é obrigatório")
    private String modoRegistro; // Ex: "SELF" ou "SHARED_DEVICE"
    
    @NotNull(message = "O horário de captura é obrigatório")
    private LocalDateTime capturadoEm;
}