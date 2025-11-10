package xyz.fream.api.global.common.dto;

import lombok.Getter;
import xyz.fream.api.global.presentation.exception.CommonException;
import xyz.fream.api.global.presentation.exception.GlobalErrorCode;

@Getter
public class PageInfo {
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final int numberOfElements;
    private final boolean first;
    private final boolean last;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final boolean empty;

    private PageInfo(int page, int size, long totalElements, int totalPages,
                     int numberOfElements) {
        validateParameters(page, size, totalElements, totalPages);

        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.numberOfElements = numberOfElements;
        this.first = (page == 0);
        this.last = (page == totalPages - 1) || (totalPages == 0);
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
        this.empty = numberOfElements == 0;
    }

    public static PageInfo of(int page, int size, long totalElements, int totalPages, int numberOfElements) {
        return new PageInfo(page, size, totalElements, totalPages, numberOfElements);
    }

    private void validateParameters(int page, int size, long totalElements, int totalPages) {
        if (page < 0) {
            throw new CommonException(GlobalErrorCode.INVALID_PAGE_NUMBER);
        }
        if (size <= 0) {
            throw new CommonException(GlobalErrorCode.INVALID_PAGE_SIZE);
        }
        if (totalElements < 0) {
            throw new CommonException(GlobalErrorCode.INVALID_TOTAL_ELEMENTS);
        }
        if (totalPages < 0) {
            throw new CommonException(GlobalErrorCode.INVALID_TOTAL_PAGES);
        }
    }
}
