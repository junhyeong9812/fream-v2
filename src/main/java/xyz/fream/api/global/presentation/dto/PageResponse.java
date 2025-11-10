package xyz.fream.api.global.presentation.dto;

import lombok.Getter;
import xyz.fream.api.global.common.dto.PageInfo;
import xyz.fream.api.global.presentation.exception.CommonException;
import xyz.fream.api.global.presentation.exception.GlobalErrorCode;

import java.util.List;
import java.util.Collections;

@Getter
public class PageResponse<T> {
    private final List<T> content;
    private final PageInfo pageInfo;

    private PageResponse(List<T> content, PageInfo pageInfo) {
        validatePageInfo(pageInfo);
        this.content = content != null ? content : Collections.emptyList();
        this.pageInfo = pageInfo;
    }

    public static <T> PageResponse<T> of(List<T> content, PageInfo pageInfo) {
        return new PageResponse<>(content, pageInfo);
    }

    private void validatePageInfo(PageInfo pageInfo) {
        if (pageInfo == null) {
            throw new CommonException(GlobalErrorCode.PAGE_INFO_NULL);
        }
    }

}
