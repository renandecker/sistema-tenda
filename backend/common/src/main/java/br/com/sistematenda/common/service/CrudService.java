package br.com.sistematenda.common.service;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.UUID;

public interface CrudService<E, D, ID> {

    D findById(ID id);

    PageResponse<D> findAll(int page, int size, String sortBy, String sortDirection);

    PageResponse<D> findAll(int page, int size);

    List<D> findAll();

    D create(D dto);

    D update(ID id, D dto);

    void delete(ID id);

    void deleteById(ID id);

    boolean existsById(ID id);

    long count();

    default Sort getSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by("id");
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending;
        return Sort.by(sortBy, direction);
    }

    default Page getPage(int page, int size, Sort sort) {
        return Page.of(page, size, sort);
    }

    default Page getPage(int page, int size) {
        return Page.of(page, size);
    }
}