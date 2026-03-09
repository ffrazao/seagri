package br.gov.df.seagri.modulo_presenca.dominio;

import br.gov.df.seagri.dominio_central.dominio.EntidadeBase;
import br.gov.df.seagri.dominio_central.dominio.PertenceOrganizacao;
import br.gov.df.seagri.dominio_central.dominio.RastreavelCriacao;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "registro_presenca")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistroPresenca extends EntidadeBase implements PertenceOrganizacao, RastreavelCriacao {

    @Column(name = "organizacao_id", nullable = false, updatable = false)
    private UUID organizacaoId;

    @Column(name = "unidade_id", nullable = false, updatable = false)
    private UUID unidadeId;

    @Column(name = "usuario_id", nullable = false, length = 64, updatable = false)
    private String usuarioId;

    @Column(name = "latitude", nullable = false, updatable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false, updatable = false)
    private Double longitude;

    @Column(name = "precisao_gps", nullable = false, updatable = false)
    private Double precisaoGps;

    // Ajustado para pontuacao_biometrica
    @Column(name = "pontuacao_biometrica", updatable = false)
    private Double pontuacaoBiometrica;

    @Column(name = "biometria_valida", updatable = false)
    private Boolean biometriaValida;

    @Column(name = "dispositivo_id", nullable = false, length = 128, updatable = false)
    private String dispositivoId;

    @Column(name = "modo_registro", nullable = false, length = 16, updatable = false)
    private String modoRegistro;

    @Column(name = "usuario_intermediario_id", length = 64, updatable = false)
    private String usuarioIntermediarioId;

    // Ajustado para pontuacao_risco
    @Setter // Adicionado para a Validação Antifraude (RFC-0003)
    @Column(name = "pontuacao_risco", updatable = false)
    private Double pontuacaoRisco;

    // Ajustado para indicadores_risco
    @Setter // Adicionado para a Validação Antifraude (RFC-0003)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "indicadores_risco", columnDefinition = "jsonb", updatable = false)
    private String indicadoresRisco;

    // O statusTécnico continua sem setter, pois é SEMPRE "RECEBIDO" (Imutável)
    @Column(name = "status_tecnico", nullable = false, length = 32, updatable = false)
    private String statusTecnico;

    @Setter // Adicionado para aprovação automática caso o GPS esteja correto
    @Column(name = "status_administrativo", nullable = false, length = 32, updatable = false)
    private String statusAdministrativo;

    @Column(name = "capturado_em", nullable = false, updatable = false)
    private LocalDateTime capturadoEm;

    @Column(name = "recebido_no_servidor_em", nullable = false, updatable = false)
    private LocalDateTime recebidoNoServidorEm;

    // Campos de criação exigidos pelo SQL (V1__schema_inicial.sql)
    @Column(name = "criado_por", nullable = false, length = 64, updatable = false)
    private String criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public RegistroPresenca(UUID organizacaoId, UUID unidadeId, String usuarioId, 
                            Double latitude, Double longitude, Double precisaoGps, 
                            String dispositivoId, String modoRegistro, LocalDateTime capturadoEm,
                            String criadoPor) {
        this.organizacaoId = organizacaoId;
        this.unidadeId = unidadeId;
        this.usuarioId = usuarioId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.precisaoGps = precisaoGps;
        this.dispositivoId = dispositivoId;
        this.modoRegistro = modoRegistro;
        this.capturadoEm = capturadoEm;
        this.criadoPor = criadoPor; // Novo campo
        
        this.recebidoNoServidorEm = LocalDateTime.now(ZoneOffset.UTC);
        this.criadoEm = this.recebidoNoServidorEm; 
        this.statusTecnico = "RECEBIDO";
        this.statusAdministrativo = "PENDENTE";
    }

    @Override
    public UUID obterOrganizacaoId() {
        return this.organizacaoId;
    }
}