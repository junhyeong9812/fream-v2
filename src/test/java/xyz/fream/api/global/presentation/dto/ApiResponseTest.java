package xyz.fream.api.global.presentation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse dto 테스트")
class ApiResponseTest {

    @Test
    @DisplayName("데이터와 함께 성공 응답을 생성할 수 있다.")
    void should_create_success_response_with_data() {
        // given
        String data = "test data";

        // when
        ApiResponse<String> response = ApiResponse.success(data);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("데이터와 메시지를 포함한 성공 응답을 생성할 수 있다.")
    void should_create_success_response_with_data_and_message() {
        // given
        String data = "test data";
        String message = "성공적으로 처리되었습니다.";

        // when
        ApiResponse<String> response = ApiResponse.success(data, message);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("메시지와 함께 실패 응답을 생성할 수 있다.")
    void should_create_fail_response_with_message() {
        // given
        String message = "처리에 실패했습니다.";

        // when
        ApiResponse<Void> response = ApiResponse.fail(message);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("에러 코드, 메시지, 데이터와 함께 실패 응답을 생성할 수 있다.")
    void should_create_error_reponse_with_error_code_and_data() {
        // given
        String errorCode = "GLOBAL_101";
        String message = "유효하지 않은 입력값";
        String data = "email field";

        // when
        ApiResponse<String> response  = ApiResponse.error(errorCode, message, data);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getTimestamp()).isNotNull();
    }
}