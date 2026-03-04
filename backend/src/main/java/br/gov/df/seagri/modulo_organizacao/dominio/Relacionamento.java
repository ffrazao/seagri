package br.gov.df.seagri.modulo_organizacao.dominio;

import br.gov.df.seagri.dominio_central.dominio.AuditoriaCompleta;
import br.gov.df.seagri.dominio_central.dominio.EntidadeBase;
import br.gov.df.seagri.dominio_central.dominio.PertenceOrganizacao;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "relacionamento")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Relacionamento extends EntidadeBase implements AuditoriaCompleta, PertenceOrganizacao {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false, updatable = false)
    private Organizacao organizacao;

    @Column(name = "sujeito_id", nullable = false, length = 64, updatable = false)
    private String sujeitoId;

    @Column(name = "objeto_id", nullable = false, length = 64, updatable = false)
    private String objetoId;

    @Setter
    @Column(name = "tipo_relacionamento", nullable = false, length = 64)
    private String tipoRelacionamento;

    @Setter
    @Column(name = "inicio_validade")
    private LocalDateTime inicioValidade;

    @Setter
    @Column(name = "fim_validade")
    private LocalDateTime fimValidade;

    @Column(name = "criado_por", nullable = false, length = 64, updatable = false)
    private String criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Setter
    @Column(name = "atualizado_por", length = 64)
    private String atualizadoPor;

    @Setter
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Relacionamento(Organizacao organizacao, String sujeitoId, String objetoId, String tipoRelacionamento, String criadoPor) {
        this.organizacao = organizacao;
        this.sujeitoId = sujeitoId;
        this.objetoId = objetoId;
        this.tipoRelacionamento = tipoRelacionamento;
        this.criadoPor = criadoPor;
        this.criadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public UUID obterOrganizacaoId() {
        return this.organizacao.getId();
    }
}