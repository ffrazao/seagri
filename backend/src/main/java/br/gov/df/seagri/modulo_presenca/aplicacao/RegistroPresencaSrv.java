package br.gov.df.seagri.modulo_presenca.aplicacao;

import br.gov.df.seagri.dominio_central.aplicacao.BaseCrudTenantSrv;
import br.gov.df.seagri.dominio_central.aplicacao.ValidadorTenant;
import br.gov.df.seagri.modulo_presenca.dominio.RegistroPresenca;
import br.gov.df.seagri.modulo_presenca.infraestrutura.RegistroPresencaDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegistroPresencaSrv extends BaseCrudTenantSrv<RegistroPresenca, UUID> {

    public RegistroPresencaSrv(RegistroPresencaDAO dao, ValidadorTenant validadorTenant) {
        super(dao, validadorTenant);
    }

    // ========================================================================
    // APLICAÇÃO DA RFC-008: Comando Único
    // ========================================================================
    @Transactional
    public RegistroPresenca registrar(UUID organizacaoId, UUID unidadeId, String usuarioId, 
                                      Double latitude, Double longitude, Double precisaoGps, 
                                      String dispositivoId, String modoRegistro, LocalDateTime capturadoEm,
                                      String criadoPor) {
        
        RegistroPresenca novoEventoBruto = new RegistroPresenca(
                organizacaoId, unidadeId, usuarioId, latitude, longitude, 
                precisaoGps, dispositivoId, modoRegistro, capturadoEm, criadoPor
        );
        
        return super.salvar(organizacaoId, novoEventoBruto);
    }

    // ========================================================================
    // BLOQUEIOS DE SEGURANÇA (RFC-008 e RFC-0005): Imutabilidade Absoluta
    // ========================================================================
    
    @Override
    @Transactional
    public void excluir(UUID organizacaoId, UUID id) {
        throw new UnsupportedOperationException(
            "Violação da RFC-008: Registros de presença são eventos brutos estritamente imutáveis e não podem ser excluídos."
        );
    }

    @Override
    @Transactional
    public RegistroPresenca salvar(UUID organizacaoId, RegistroPresenca entidade) {
        if (entidade.getId() != null) {
            throw new UnsupportedOperationException(
                "Violação da RFC-0005: Atualização de registro de presença não é permitida. O evento bruto é imutável."
            );
        }
        
        return super.salvar(organizacaoId, entidade);
    }
}