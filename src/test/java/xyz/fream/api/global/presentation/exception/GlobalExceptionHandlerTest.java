package xyz.fream.api.global.presentation.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@WebFluxTest
@ContextConfiguration(classes = {
        GlobalExceptionHandler.class,
        GlobalExceptionHandlerTest.TestController.class,
        GlobalExceptionHandlerTest.TestConfig.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("GlobalException 발생 시 ErrorResponse 형식으로 응답한다.")
    void should_return_error_response_when_global_exception_thrown() {
        // when & then
        webTestClient.get()
                .uri("/test/global-exception")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("GLOBAL_300")
                .jsonPath("$.message").isEqualTo("요청한 리소스를 찾을 수 없습니다.")
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.path").isEqualTo("/test/global-exception")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @DisplayName("GlobalException에 커스텀 메시지가 있으면 해당 메시지를 반환한다.")
    void should_return_custom_message_when_global_exception_has_custom_message() {
        // when & then
        webTestClient.get()
                .uri("/test/custom-message")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("GLOBAL_101")
                .jsonPath("$.message").isEqualTo("이메일 형식이 올바르지 않습니다.")
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("Validation 예외 발생 시 400 에러로 응답한다.")
    void should_return_400_when_validation_exception_thrown() {
        // given
        TestRequest request = new TestRequest("");

        // when & then
        webTestClient.post()
                .uri("/test/validation")
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("GLOBAL_101")
                .jsonPath("$.message").exists()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("일반 RuntimeException 발생 시 500 에러로 응답한다.")
    void should_return_500_when_unhandled_exception_thrown() {
        // when & then
        webTestClient.get()
                .uri("/test/runtime-exception")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.code").isEqualTo("GLOBAL_001")
                .jsonPath("$.message").isEqualTo("서버 내부 오류가 발생했습니다.")
                .jsonPath("$.status").isEqualTo(500);
    }

    @Test
    @DisplayName("다양한 HTTP 상태 코드를 가진 GlobalException을 올바르게 처리한다.")
    void should_handle_various_httpStatusCodes() {
        // 400
        webTestClient.get()
                .uri("/test/bad-request")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);

        // 409
        webTestClient.get()
                .uri("/test/conflict")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409);

        // 503
        webTestClient.get()
                .uri("/test/service-unavailable")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503);
    }

    // test Configuration
    @Configuration
    @Import(GlobalExceptionHandler.class)
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/test/global-exception")
        public Mono<String> throwGlobalException() {
            return Mono.error(new TestException(GlobalErrorCode.RESOURCE_NOT_FOUND));
        }

        @GetMapping("/test/custom-message")
        public Mono<String> throwCustomMessageException() {
            return Mono.error(new TestException(
                    GlobalErrorCode.INVALID_INPUT_VALUE,
                    "이메일 형식이 올바르지 않습니다."
            ));
        }

        @PostMapping("/test/validation")
        public Mono<String> throwValidationException(@Valid @RequestBody TestRequest testRequest) {
            return Mono.just("ok");
        }

        @GetMapping("/test/runtime-exception")
        public Mono<String> throwRuntimeException() {
            return Mono.error(new RuntimeException("Unexpected error"));
        }

        @GetMapping("/test/bad-request")
        public Mono<String> throwBadRequest() {
            return Mono.error(new TestException(GlobalErrorCode.BAD_REQUEST));
        }

        @GetMapping("/test/conflict")
        public Mono<String> throwConflict() {
            return Mono.error(new TestException(GlobalErrorCode.DUPLICATE_RESOURCE));
        }

        @GetMapping("/test/service-unavailable")
        public Mono<String> throwServiceUnavailable() {
            return Mono.error(new TestException(GlobalErrorCode.SERVICE_UNAVAILABLE));
        }
    }

    // test exception
    static class TestException extends GlobalException {
        public TestException(ErrorCode errorCode) {
            super(errorCode);
        }

        public TestException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }
    }

    // test request
    record TestRequest(@NotBlank(message = "값은 필수입니다.") String value) {}
}