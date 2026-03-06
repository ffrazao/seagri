package br.gov.df.seagri.modulo_presenca.aplicacao;

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

    public RegistroPresencaSrv(RegistroPresencaDAO registroPresencaDAO) {
        this.registroPresencaDAO = registroPresencaDAO;
    }

    // Método responsável por registrar a presença respeitando a imutabilidade (RFC-008)
    @Transactional
    public RegistroPresenca registrar(UUID organizacaoId, UUID unidadeId, String usuarioId, 
                                      Double latitude, Double longitude, Double precisaoGps, 
                                      String dispositivoId, String modoRegistro, 
                                      LocalDateTime capturadoEm, String criadoPor) {
                                          
        // Utilizando exclusivamente o construtor blindado da entidade.
        // Observação: Não precisamos mais setar 'statusTecnico', 'statusAdministrativo' 
        // ou 'recebidoNoServidorEm' aqui, pois o seu construtor já faz isso com perfeição!
        RegistroPresenca presenca = new RegistroPresenca(
                organizacaoId,
                unidadeId,
                usuarioId,
                latitude,
                longitude,
                precisaoGps,
                dispositivoId,
                modoRegistro,
                capturadoEm,
                criadoPor
        );
        
        return registroPresencaDAO.save(presenca);
    }

    // Método responsável por devolver a lista de pontos do usuário logado
    @Transactional(readOnly = true)
    public List<RegistroPresenca> buscarHistoricoPorUsuario(String usuarioId) {
        return registroPresencaDAO.buscarPorUsuario(usuarioId); 
    }
}