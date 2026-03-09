package br.gov.df.seagri.modulo_presenca.aplicacao;

import br.gov.df.seagri.dominio_central.aplicacao.GeolocalizacaoSrv;
import br.gov.df.seagri.modulo_organizacao.aplicacao.UnidadeSrv;
import br.gov.df.seagri.modulo_organizacao.dominio.Unidade;
import br.gov.df.seagri.modulo_presenca.dominio.RegistroPresenca;
import br.gov.df.seagri.modulo_presenca.infraestrutura.RegistroPresencaDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RegistroPresencaSrv {

    private final RegistroPresencaDAO registroPresencaDAO;
    private final GeolocalizacaoSrv geolocalizacaoSrv;
    private final UnidadeSrv unidadeSrv;

    public RegistroPresencaSrv(RegistroPresencaDAO registroPresencaDAO, 
                               GeolocalizacaoSrv geolocalizacaoSrv, 
                               UnidadeSrv unidadeSrv) {
        this.registroPresencaDAO = registroPresencaDAO;
        this.geolocalizacaoSrv = geolocalizacaoSrv;
        this.unidadeSrv = unidadeSrv;
    }

    @Transactional
    public RegistroPresenca registrar(UUID organizacaoId, UUID unidadeId, String usuarioId,
                                      Double latitude, Double longitude, Double precisaoGps,
                                      String dispositivoId, String modoRegistro,
                                      LocalDateTime capturadoEm, String criadoPor) {

        // 1. Instanciação imutável (RFC-008: Status Técnico=RECEBIDO, Administrativo=PENDENTE)
        RegistroPresenca presenca = new RegistroPresenca(
                organizacaoId, unidadeId, usuarioId, latitude, longitude,
                precisaoGps, dispositivoId, modoRegistro, capturadoEm, criadoPor
        );

        // 2. Busca a Unidade passando o organizacaoId e o unidadeId para garantir o isolamento Multi-Tenant!
        Unidade unidade = unidadeSrv.buscarPorId(organizacaoId, unidadeId);

        // 3. Aplica a Validação Geográfica com os atributos reais da Unidade
        if (unidade.getCentroGeoLat() != null && unidade.getCentroGeoLng() != null) {
            
            double distancia = geolocalizacaoSrv.calcularDistanciaMetros(
                    latitude, longitude,
                    unidade.getCentroGeoLat(), unidade.getCentroGeoLng()
            );

            // Usa o raio cadastrado na unidade ou 200 metros como fallback de segurança
            double raioPermitido = unidade.getRaioGeoMetros() != null ? unidade.getRaioGeoMetros() : 200.0;

            if (distancia > raioPermitido) {
                // Fora da geofence: Eleva a pontuação de risco e anota a infração em JSONB
                // O status permanece PENDENTE (requer aprovação da chefia)
                presenca.setPontuacaoRisco(80.0);
                presenca.setIndicadoresRisco("{\"fora_do_raio_permitido\": true, \"distancia_metros\": " + Math.round(distancia) + "}");
            } else {
                // Dentro da localidade esperada: Risco zero e evento administrativamente válido!
                presenca.setPontuacaoRisco(0.0);
                presenca.setIndicadoresRisco("{}");
                presenca.setStatusAdministrativo("VALIDO");
            }
        } else {
            // Unidade não possui GPS configurado. Ponto é salvo, mas marcado com risco baixo/pendente.
            presenca.setPontuacaoRisco(10.0);
            presenca.setIndicadoresRisco("{\"aviso\": \"unidade_sem_configuracao_gps\"}");
        }

        return registroPresencaDAO.save(presenca);
    }

    @Transactional(readOnly = true)
    public List<RegistroPresenca> buscarHistoricoPorUsuario(String usuarioId) {
        return registroPresencaDAO.buscarPorUsuario(usuarioId);
    }
}