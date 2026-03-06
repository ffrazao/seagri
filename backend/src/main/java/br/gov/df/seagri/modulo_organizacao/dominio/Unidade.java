package br.gov.df.seagri.modulo_organizacao.dominio;

import br.gov.df.seagri.dominio_central.dominio.AuditoriaCompleta;
import br.gov.df.seagri.dominio_central.dominio.EntidadeBase;
import br.gov.df.seagri.dominio_central.dominio.PertenceOrganizacao;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "unidade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Unidade extends EntidadeBase implements AuditoriaCompleta, PertenceOrganizacao {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false, updatable = false)
    private Organizacao organizacao;

    @Setter
    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Setter
    @Column(name = "tipo_geometria", nullable = false, length = 16)
    private String tipoGeometria;

    @Setter
    @Column(name = "centro_geo_lat")
    private Double centroGeoLat;

    @Setter
    @Column(name = "centro_geo_lng")
    private Double centroGeoLng;

    @Setter
    @Column(name = "raio_geo_metros")
    private Integer raioGeoMetros;

    @JdbcTypeCode(SqlTypes.JSON)
    @Setter
    @Column(name = "poligono_geo", columnDefinition = "jsonb")
    private String poligonoGeo;

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

    public Unidade(Organizacao organizacao, String nome, String tipoGeometria, String criadoPor) {
        this.organizacao = organizacao;
        this.nome = nome;
        this.tipoGeometria = tipoGeometria;
        this.criadoPor = criadoPor;
        this.criadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public UUID obterOrganizacaoId() {
        return this.organizacao.getId();
    }
}