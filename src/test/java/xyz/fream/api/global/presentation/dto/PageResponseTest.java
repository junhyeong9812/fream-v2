package xyz.fream.api.global.presentation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.fream.api.global.common.dto.PageInfo;
import xyz.fream.api.global.presentation.exception.CommonException;

import java.util.List;
import java.util.Collections;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PageResponse dto 테스트")
class PageResponseTest {
    
    @Test
    @DisplayName("PageResponse를 생성할 수 있다.")
    void should_create_page_response() {
        // given
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PageInfo pageInfo = PageInfo.of(0, 10, 3, 1, 3);

        // when
        PageResponse<String> response = PageResponse.of(content, pageInfo);

        // then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent()).containsExactly("item1", "item2","item3");
        assertThat(response.getPageInfo()).isEqualTo(pageInfo);
    }

    @Test
    @DisplayName("content가 null이면 빈 리스트로 초기화된다")
    void should_initialize_empty_list_when_content_is_null() {
        // given
        PageInfo pageInfo = PageInfo.of(0, 10, 0, 0, 0);

        // when
        PageResponse<String> response = PageResponse.of(null, pageInfo);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPageInfo()).isEqualTo(pageInfo);
    }

    @Test
    @DisplayName("빈 리스트로 PageResponse를 생성 할 수 있다.")
    void should_create_page_response_with_empty_list() {
        // given
        List<String> content = Collections.emptyList();
        PageInfo pageInfo = PageInfo.of(0, 10, 0, 0, 0);

        // when
        PageResponse<String> response = PageResponse.of(content, pageInfo);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPageInfo().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("pageInfo가 null이면 예외가 발생한다.")
    void should_throw_exception_when_page_info_null() {
        // given
        List<String> content = Arrays.asList("items");

        // when & then
        assertThatThrownBy(() -> PageResponse.of(content, null))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("페이지 정보는 null이 될 수 없습니다.");
    }
}