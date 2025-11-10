package xyz.fream.api.global.presentation.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalException 테스트")
class GlobalExceptionTest {

    @Test
    @DisplayName("ErrorCode만으로 예외를 생성할 수 있다.")
    void should_create_exception_with_error_codeOnly() {
        // given
        ErrorCode errorCode = GlobalErrorCode.RESOURCE_NOT_FOUND;

        // when
        TestException exception = new TestException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("ErrorCode와 커스텀 메시지로 예외를 생성할 수 있다.")
    void should_create_exception_with_custom_message() {
        // given
        ErrorCode errorCode = GlobalErrorCode.RESOURCE_NOT_FOUND;
        String customMessage = "이메일 형식이 올바르지 않습니다.";

        // when
        TestException exception = new TestException(errorCode, customMessage);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getMessage()).isNotEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("ErrorCode와 원인 예외로 예외를 생성할 수 있다.")
    void should_create_exception_with_cause() {
        // given
        ErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        Throwable cause = new RuntimeException("원인 예외");

        // when
        TestException exception = new TestException(errorCode, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("ErrorCode, 커스텀 메시지, 원인 예외로 예외를 생성할 수 있다.")
    void should_create_exception_with_all_parameters() {
        // given
        ErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        String customMessage = "데이터베이스 연결 실패";
        Throwable cause = new RuntimeException("Connection timeout");

        // when
        TestException exception = new TestException(errorCode, customMessage, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    //test용 globalException 구현체
    static class TestException extends GlobalException {
        public TestException(ErrorCode errorCode) {
            super(errorCode);
        }

        public TestException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }

        public TestException(ErrorCode errorCode, Throwable cause) {
            super(errorCode, cause);
        }

        public TestException(ErrorCode errorCode, String message, Throwable cause) {
            super(errorCode, message, cause);
        }
    }
}