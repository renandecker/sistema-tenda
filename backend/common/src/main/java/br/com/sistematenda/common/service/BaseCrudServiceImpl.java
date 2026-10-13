package br.com.sistematenda.common.service;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.mapper.BaseMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public abstract class BaseCrudServiceImpl<E extends BaseEntity, D, ID> implements CrudService<E, D, ID> {

    @Inject
    protected BaseMapper<E, D> mapper;

    protected abstract PanacheRepositoryBase<E, ID> getRepository();

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

    @Override
    public List<D> findAll() {
        return getRepository().find("deleted = false").list().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public D create(D dto) {
        E entity = mapper.toEntity(dto);
        if (entity.id == null) {
            entity.id = generateId();
        }
        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public D update(ID id, D dto) {
        E entity = getRepository().findById(id);
        if (entity == null || entity.deleted) {
            throw new NotFoundException(getEntityClass(), id);
        }
        mapper.updateEntityFromDTO(dto, entity);
        getRepository().persist(entity);
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

    protected abstract Class<E> getEntityClass();
}