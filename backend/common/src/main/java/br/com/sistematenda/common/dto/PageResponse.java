package br.com.sistematenda.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean first;
    private boolean last;
    private boolean empty;

    public static <T> PageResponse<T> of(List<T> content, long totalElements, int currentPage, int pageSize) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return new PageResponse<>(
                content,
                totalElements,
                totalPages,
                currentPage,
                pageSize,
                currentPage == 0,
                currentPage >= totalPages - 1,
                content.isEmpty()
        );
    }
}