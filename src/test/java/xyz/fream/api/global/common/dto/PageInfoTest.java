package xyz.fream.api.global.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.fream.api.global.presentation.exception.CommonException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PageInfo 기능 테스트")
class PageInfoTest {

    @Test
    @DisplayName("PageInfo를 생성할 수 있다.")
    void should_create_page_info() {

    }

    @Test
    @DisplayName("첫 페이지는 first가 true다.")
    void should_be_first_page_when_page_is_zero() {
        // given
        int page = 0;
        int size = 10;
        long totalElements = 100;
        int totalPages = 10;
        int numberOfElements = 10;

        // when
        PageInfo pageInfo = PageInfo.of(page, size, totalElements, totalPages, numberOfElements);

        // then
        assertThat(pageInfo.getPage()).isEqualTo(page);
        assertThat(pageInfo.getSize()).isEqualTo(size);
        assertThat(pageInfo.getTotalElements()).isEqualTo(totalElements);
        assertThat(pageInfo.getTotalPages()).isEqualTo(totalPages);
        assertThat(pageInfo.getNumberOfElements()).isEqualTo(numberOfElements);
    }

    @Test
    @DisplayName("마지막 페이지는 last가 true다.")
    void should_be_last_page_when_page_is_last_page() {
        // when
        PageInfo pageInfo = PageInfo.of(0, 10, 100, 10, 10);

        // then
        assertThat(pageInfo.isFirst()).isTrue();
        assertThat(pageInfo.isLast()).isFalse();
    }

    @Test
    @DisplayName("다음 페이지가 있으면 hasNext가 true다.")
    void should_have_next_page_when_not_last_page() {
        // when
        PageInfo pageInfo = PageInfo.of(0, 10, 100, 10, 10);

        // then
        assertThat(pageInfo.isFirst()).isTrue();
        assertThat(pageInfo.isLast()).isFalse();
    }

    @Test
    @DisplayName("이전 페이지가 있으면 hasPrevious가 true다.")
    void should_have_previous_page_when_not_first_page() {
        // when
        PageInfo pageInfo = PageInfo.of(1, 10, 100, 10, 10);

        // then
        assertThat(pageInfo.isHasPrevious()).isTrue();
    }

    @Test
    @DisplayName("데이터가 없으면 empty가 true다")
    void should_be_empty_when_no_elements() {
        // when
        PageInfo pageInfo = PageInfo.of(0, 10, 0, 0, 0);

        // then
        assertThat(pageInfo.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("page가 음수면 예외가 발생한다.")
    void should_throw_exception_when_page_is_negative() {
        // when & then
        assertThatThrownBy(() -> PageInfo.of(-1, 10, 100, 10, 10))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("페이지 번호는 0보다 크거나 같아야 합니다.");
    }

    @Test
    @DisplayName("size가 0이하면 예외가 발생한다.")
    void should_throw_exception_when_size_is_zero_or_negative() {
        // when & then
        assertThatThrownBy(() -> PageInfo.of(0, 0, 100, 10, 10))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("페이지 크기는 0보다 커야 합니다.");

        assertThatThrownBy(() -> PageInfo.of(0, -1, 100, 10, 10))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("페이지 크기는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("TotalElements가 음수면 예외가 발생한다.")
    void should_throw_exception_when_total_elements_is_negative() {
        // when & then
        assertThatThrownBy(() -> PageInfo.of(0, 10, -1, 10, 10))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("전체 요소 수는 0보다 크거나 같아야 합니다.");
    }

    @Test
    @DisplayName("totalPage가 음수면 예외가 발생한다.")
    void should_throw_exception_when_total_pages_is_negative(){
        // when & then
        assertThatThrownBy(() -> PageInfo.of(0, 10, 100, -1, 10))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("전체 페이지 수는 0보다 크거나 같아야 합니다.");
    }
}