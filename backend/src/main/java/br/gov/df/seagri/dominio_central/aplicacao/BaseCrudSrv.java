package br.gov.df.seagri.dominio_central.aplicacao;

import br.gov.df.seagri.dominio_central.dominio.EntidadeBase;
import br.gov.df.seagri.dominio_central.infraestrutura.BaseDAO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class BaseCrudSrv<T extends EntidadeBase, ID> implements CrudSrv<T, ID> {

    protected final BaseDAO<T, ID> dao;

    public BaseCrudSrv(BaseDAO<T, ID> dao) {
        this.dao = dao;
    }

    @Override
    @Transactional
    public T salvar(T entidade) { return dao.save(entidade); }

    @Override
    public T buscarPorId(ID id) {
        return dao.findById(id).orElseThrow(() -> new IllegalArgumentException("Registro não encontrado: " + id));
    }

    @Override
    @Transactional
    public void excluir(ID id) { dao.deleteById(id); }

    @Override
    public List<T> buscarTudo() { return dao.findAll(); }

    @Override
    public Page<T> buscarTudo(Pageable pageable) { return dao.findAll(pageable); }

    @Override
    public List<T> buscarTudoComFiltro(Specification<T> filtro) { return dao.findAll(filtro); }

    @Override
    public Page<T> buscarTudoComFiltro(Specification<T> filtro, Pageable pageable) { return dao.findAll(filtro, pageable); }
}