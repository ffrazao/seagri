package br.gov.df.seagri.modulo_presenca.aplicacao;

import br.gov.df.seagri.dominio_central.aplicacao.GeolocalizacaoSrv;
import br.gov.df.seagri.dominio_central.infraestrutura.ArmazenamentoLocalSrv;
import br.gov.df.seagri.modulo_organizacao.aplicacao.UnidadeSrv;
import br.gov.df.seagri.modulo_organizacao.dominio.Unidade;
import br.gov.df.seagri.modulo_presenca.dominio.RegistroPresenca;
import br.gov.df.seagri.modulo_presenca.infraestrutura.RegistroPresencaDAO;
import br.gov.df.seagri.modulo_presenca.web.dto.RegistroPresencaRequestDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service 
public class RegistroPresencaSrv {

    private final RegistroPresencaDAO registroPresencaDAO;
    private final UnidadeSrv unidadeSrv;
    private final GeolocalizacaoSrv geolocalizacaoSrv;
    private final ArmazenamentoLocalSrv armazenamentoLocalSrv;

    public RegistroPresencaSrv(RegistroPresencaDAO registroPresencaDAO, 
                               UnidadeSrv unidadeSrv, 
                               GeolocalizacaoSrv geolocalizacaoSrv,
                               ArmazenamentoLocalSrv armazenamentoLocalSrv) {
        this.registroPresencaDAO = registroPresencaDAO;
        this.unidadeSrv = unidadeSrv;
        this.geolocalizacaoSrv = geolocalizacaoSrv;
        this.armazenamentoLocalSrv = armazenamentoLocalSrv;
    }

    @Transactional
    public RegistroPresenca registrar(UUID organizacaoId, String userId, RegistroPresencaRequestDTO dto) {

        // Rastreador visual no log para confirmar a chegada da imagem Base64
        if (dto.getFotoBase64() != null) {
            System.out.println("📸 SUCESSO! Foto recebida no Java. Tamanho: " + dto.getFotoBase64().length() + " caracteres.");
        } else {
            System.out.println("❌ AVISO: A foto chegou NULA. O usuário negou a permissão da câmera.");
        }

        // Busca a unidade passando o Tenant (Organização) e o ID da Unidade
        Unidade unidade = unidadeSrv.buscarPorId(organizacaoId, dto.getUnidadeId());

        String statusAdministrativo = "VALIDO";
        Double pontuacaoRisco = 0.0;
        
        // Usaremos uma Lista em vez de StringBuilder para facilitar a conversão para JSON (exigência do PostgreSQL JSONB)
        java.util.List<String> indicadores = new java.util.ArrayList<>();

        // 1. Validação de GPS (O usuário recusou a localização?)
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            statusAdministrativo = "PENDENTE";
            pontuacaoRisco += 50.0;
            indicadores.add("SEM_GPS"); // Adiciona na lista sem os colchetes
        } else {
            // Se tem GPS e a unidade possui centro geográfico configurado, calcula a distância
            if (unidade.getCentroGeoLat() != null && unidade.getCentroGeoLng() != null) {
                double distancia = geolocalizacaoSrv.calcularDistanciaMetros(
                        dto.getLatitude(), dto.getLongitude(),
                        unidade.getCentroGeoLat(), unidade.getCentroGeoLng()
                );
                
                // Se estiver muito longe da unidade, penaliza
                if (distancia > unidade.getRaioGeoMetros()) {
                    statusAdministrativo = "PENDENTE";
                    pontuacaoRisco += 40.0;
                    indicadores.add("FORA_DO_RAIO");
                }
            } else {
                indicadores.add("UNIDADE_SEM_GEO");
            }
        }

        // 2. Validação de Biometria (O usuário recusou a câmera?)
        UUID referenciaBiometrica = armazenamentoLocalSrv.salvarFotoBase64(dto.getFotoBase64());
        
        if (referenciaBiometrica == null) {
            statusAdministrativo = "PENDENTE";
            pontuacaoRisco += 50.0; // Soma mais 50 pontos de risco
            indicadores.add("SEM_FOTO");
        }

        // 3. Converte a Lista do Java para uma String em formato JSON Array válido para o PostgreSQL
        String jsonIndicadores = "[]";
        if (!indicadores.isEmpty()) {
            jsonIndicadores = "[\"" + String.join("\", \"", indicadores) + "\"]";
        }

        // Cria o evento bruto e imutável de presença (Sempre preservado conforme RFC-008)
        RegistroPresenca registro = new RegistroPresenca(
                organizacaoId,
                unidade.getId(),
                userId,
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getPrecisaoGps(),
                dto.getDispositivoId(),
                dto.getModoRegistro(),
                dto.getCapturadoEm(),
                "RECEBIDO"
        );

        // Aplica os resultados da análise antifraude
        registro.setStatusAdministrativo(statusAdministrativo);
        registro.setPontuacaoRisco(pontuacaoRisco);
        
        // Envia o JSON válido (Ex: '["SEM_GPS", "SEM_FOTO"]') para a coluna JSONB
        registro.setIndicadoresRisco(jsonIndicadores); 

        // Vincula o UUID da foto salva ao registro de banco de dados, se existir
        if (referenciaBiometrica != null) {
            registro.registrarEvidenciaBiometrica(referenciaBiometrica, Double.valueOf(0.0), Boolean.FALSE);
        }

        return registroPresencaDAO.save(registro);
    }    
    // Método fiel ao nome declarado na sua interface RegistroPresencaDAO
    public List<RegistroPresenca> buscarPorUsuario(String usuarioId) {
        return registroPresencaDAO.buscarPorUsuario(usuarioId);
    }

    // Método que garante a segurança e busca os bytes da foto
    public byte[] buscarFotoBiometrica(UUID organizacaoId, String usuarioId, UUID presencaId) {
        // Busca o evento bruto no banco
        RegistroPresenca presenca = registroPresencaDAO.findById(presencaId)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"));

        // Segurança: O usuário só pode ver a sua própria foto dentro da sua organização
        if (!presenca.getUsuarioId().equals(usuarioId) || !presenca.getOrganizacaoId().equals(organizacaoId)) {
            throw new RuntimeException("Acesso negado à evidência biométrica");
        }

        return armazenamentoLocalSrv.recuperarFoto(presenca.getReferenciaBiometrica());
    }

}