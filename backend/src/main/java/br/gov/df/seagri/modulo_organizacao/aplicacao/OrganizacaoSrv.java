package br.gov.df.seagri.modulo_organizacao.aplicacao;

import br.gov.df.seagri.modulo_organizacao.dominio.Organizacao;
import br.gov.df.seagri.modulo_organizacao.infraestrutura.OrganizacaoDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizacaoSrv {

    private final OrganizacaoDAO organizacaoDAO;

    public OrganizacaoSrv(OrganizacaoDAO organizacaoDAO) {
        this.organizacaoDAO = organizacaoDAO;
    }

    @Transactional
    public Organizacao criarOrganizacao(String nome, String criadoPor) {
        Organizacao novaOrganizacao = new Organizacao(nome, criadoPor);
        return organizacaoDAO.save(novaOrganizacao);
    }

    public Organizacao buscarPorId(UUID id) {
        return organizacaoDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organização não encontrada: " + id));
    }
}
