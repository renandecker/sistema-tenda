package br.com.sistematenda.common.service;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.TipoAcaoAuditoria;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.mapper.BaseMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public abstract class BaseCrudServiceImpl<E extends BaseEntity, D, ID> implements CrudService<E, D, ID> {

    @Inject
    protected BaseMapper<E, D> mapper;

    @Inject
    protected AuditLogService auditLogService;

    protected abstract PanacheRepositoryBase<E, ID> getRepository();

    protected abstract String getEntidadeNome();

    @Override
    public D findById(ID id) {
        E entity = getRepository().findById(id);
        if (entity == null || entity.deleted) {
            throw new NotFoundException(getEntityClass(), id);
        }
        return mapper.toDTO(entity);
    }

    @Override
    public PageResponse<D> findAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = getSort(sortBy, sortDirection);
        Page pageable = getPage(page, size, sort);

        PanacheQuery<E> query = getRepository().find("deleted = false", sort);
        query.page(pageable);

        List<D> content = query.list().stream()
                .map(mapper::toDTO)
                .toList();

        long totalElements = getRepository().count("deleted = false");

        return PageResponse.of(content, totalElements, page, size);
    }

    @Override
    public PageResponse<D> findAll(int page, int size) {
        return findAll(page, size, null, null);
    }

    protected PageResponse<D> toDtoPage(PageResponse<E> page) {
        return PageResponse.of(
                page.getContent().stream().map(mapper::toDTO).toList(),
                page.getTotalElements(),
                page.getCurrentPage(),
                page.getPageSize());
    }

    @Override
    public List<D> findAll() {
        return getRepository().find("deleted = false").list().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public D create(@Valid D dto) {
        E entity = mapper.toEntity(dto);
        antesDePersistir(entity);
        getRepository().persist(entity);
        logAuditoria(TipoAcaoAuditoria.CRIAR, entity.id, "Criado: " + getDescricaoEntidade(entity));
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public D update(ID id, @Valid D dto) {
        E entity = getRepository().findById(id);
        if (entity == null || entity.deleted) {
            throw new NotFoundException(getEntityClass(), id);
        }
        mapper.updateEntityFromDTO(dto, entity);
        getRepository().persist(entity);
        logAuditoria(TipoAcaoAuditoria.EDITAR, entity.id, "Atualizado: " + getDescricaoEntidade(entity));
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(ID id) {
        E entity = getRepository().findById(id);
        if (entity == null || entity.deleted) {
            throw new NotFoundException(getEntityClass(), id);
        }
        entity.deleted = true;
        getRepository().persist(entity);
        logAuditoria(TipoAcaoAuditoria.EXCLUIR, entity.id, "Excluído: " + getDescricaoEntidade(entity));
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        delete(id);
    }

    @Override
    public boolean existsById(ID id) {
        E entity = getRepository().findById(id);
        return entity != null && !entity.deleted;
    }

    @Override
    public long count() {
        return getRepository().count("deleted = false");
    }

    protected UUID generateId() {
        return UUID.randomUUID();
    }

    protected void antesDePersistir(E entity) {
    }

    protected abstract Class<E> getEntityClass();

    protected String getDescricaoEntidade(E entity) {
        return entity.getClass().getSimpleName() + " ID: " + entity.id;
    }

    protected void logAuditoria(TipoAcaoAuditoria acao, UUID registroId, String descricao) {
        try {
            UUID usuarioId = getCurrentUserId();
            if (usuarioId != null) {
                auditLogService.logActionWithStatus(usuarioId, acao, getEntidadeNome(), registroId, descricao, null, null, true);
            }
        } catch (Exception e) {
        }
    }

    protected UUID getCurrentUserId() {
        return null;
    }
}