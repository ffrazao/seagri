package br.gov.df.seagri.modulo_organizacao.dominio;

import br.gov.df.seagri.dominio_central.dominio.EntidadeBase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "convite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Convite extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false, updatable = false)
    private Organizacao organizacao;

    @Column(name = "codigo", nullable = false, length = 32, unique = true)
    private String codigo;

    // Agora alinhado perfeitamente com a coluna renomeada no banco
    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Setter
    @Column(name = "usado", nullable = false)
    private Boolean usado;

    @Column(name = "criado_por", nullable = false, length = 64, updatable = false)
    private String criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    // Padrão de auditoria mantido conforme RFC-0005
    @Setter
    @Column(name = "atualizado_por", length = 64)
    private String atualizadoPor;

    @Setter
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Convite(Organizacao organizacao, String codigo, LocalDateTime dataExpiracao, String criadoPor) {
        this.organizacao = organizacao;
        this.codigo = codigo;
        this.dataExpiracao = dataExpiracao;
        this.criadoPor = criadoPor;
        this.criadoEm = LocalDateTime.now();
        this.usado = false;
    }

    public boolean isValido() {
        return !this.usado && this.dataExpiracao.isAfter(LocalDateTime.now());
    }
}