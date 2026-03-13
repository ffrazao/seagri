package br.gov.df.seagri.modulo_organizacao.dominio;

import br.gov.df.seagri.dominio_central.dominio.AuditoriaCompleta;
import br.gov.df.seagri.dominio_central.dominio.EntidadeBase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "alocacao_unidade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlocacaoUnidade extends EntidadeBase implements AuditoriaCompleta {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vinculo_usuario_id", nullable = false, updatable = false)
    private VinculoUsuario vinculoUsuario;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_id", nullable = false, updatable = false)
    private Unidade unidade;

    @Setter
    @Column(name = "papel_operacional", nullable = false, length = 32)
    private String papelOperacional;

    @Setter
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Setter
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Setter
    @Column(name = "data_fim")
    private LocalDateTime dataFim;

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

    public AlocacaoUnidade(VinculoUsuario vinculoUsuario, Unidade unidade, String papelOperacional, String criadoPor) {
        this.vinculoUsuario = vinculoUsuario;
        this.unidade = unidade;
        this.papelOperacional = papelOperacional;
        this.status = "ATIVO";
        this.dataInicio = LocalDateTime.now(ZoneOffset.UTC);
        this.criadoPor = criadoPor;
        this.criadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }
    
    // Método auxiliar para verificar se a alocação está vigente hoje
    public boolean isVigente() {
        if (!"ATIVO".equals(this.status)) return false;
        LocalDateTime agora = LocalDateTime.now(ZoneOffset.UTC);
        if (agora.isBefore(this.dataInicio)) return false;
        return this.dataFim == null || agora.isBefore(this.dataFim);
    }
}