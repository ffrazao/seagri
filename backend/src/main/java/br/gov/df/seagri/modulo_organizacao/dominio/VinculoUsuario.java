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
@Table(name = "vinculo_usuario", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"usuario_id", "organizacao_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VinculoUsuario extends EntidadeBase implements AuditoriaCompleta, PertenceOrganizacao {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false, updatable = false)
    private Organizacao organizacao;

    @Column(name = "usuario_id", nullable = false, length = 64, updatable = false)
    private String usuarioId;

    @Setter
    @Column(name = "papel", nullable = false, length = 32)
    private String papel;

    @Setter
    @Column(name = "status", nullable = false, length = 32)
    private String status;

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

    public VinculoUsuario(Organizacao organizacao, String usuarioId, String papel, String criadoPor) {
        this.organizacao = organizacao;
        this.usuarioId = usuarioId;
        this.papel = papel;
        this.status = "ATIVO";
        this.criadoPor = criadoPor;
        this.criadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public UUID obterOrganizacaoId() {
        return this.organizacao.getId();
    }
}