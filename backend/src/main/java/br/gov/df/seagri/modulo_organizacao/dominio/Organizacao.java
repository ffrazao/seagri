package br.gov.df.seagri.modulo_organizacao.dominio;

import br.gov.df.seagri.dominio_central.dominio.AuditoriaCompleta;
import br.gov.df.seagri.dominio_central.dominio.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "organizacao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organizacao extends EntidadeBase implements AuditoriaCompleta {

    @Setter
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Setter
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Setter
    @Column(name = "criado_por", nullable = false, length = 64, updatable = false)
    private String criadoPor;

    @Setter
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Setter
    @Column(name = "atualizado_por", length = 64)
    private String atualizadoPor;

    @Setter
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Organizacao(String nome, String criadoPor) {
        this.nome = nome;
        this.status = "ATIVO";
        this.criadoPor = criadoPor;
        this.criadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }
}