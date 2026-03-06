package br.gov.df.seagri.dominio_central.web;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Getter
@Builder
public class ApiResponse<T> {
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
    
    private int status;
    private String message;
    private T payload;
    private List<String> errors;
}
