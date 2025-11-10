package xyz.fream.api.global.presentation.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xyz.fream.api.global.presentation.dto.ErrorResponse;

/**
 * WebFlux 전역 예외 처리기
 * */
@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ErrorResponse errorResponse = createErrorResponse(exchange, ex);
        logException(ex, errorResponse);

        exchange.getResponse().setStatusCode(HttpStatus.valueOf(errorResponse.getStatus()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse().writeWith(
                Mono.fromSupplier(() -> {
                    try {
                        byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
                        return exchange.getResponse().bufferFactory().wrap(bytes);
                    } catch (JsonProcessingException e) {
                        log.error("에러 응답 JSON 직렬화에 실패했습니다.", e);
                        return exchange.getResponse().bufferFactory().wrap(new byte[0]);
                    }
                })
        );
    }

    private ErrorResponse createErrorResponse(ServerWebExchange exchange, Throwable ex) {
        String path = exchange.getRequest().getPath().value();

        if (ex instanceof GlobalException globalException) {
            var errorCode = globalException.getErrorCode();
            String message = globalException.getMessage() != null
                    ? globalException.getMessage()
                    : errorCode.getMessage();

            return ErrorResponse.of(
                    errorCode.getCode(),
                    message,
                    errorCode.getStatus(),
                    path
            );
        }

        if (ex instanceof WebExchangeBindException bindException) {
            String message = bindException.getBindingResult()
                    .getAllErrors()
                    .stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());

            return ErrorResponse.of(
                    GlobalErrorCode.INVALID_INPUT_VALUE.getCode(),
                    message,
                    HttpStatus.BAD_REQUEST.value(),
                    path
            );
        }

        return ErrorResponse.of(
                GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                path
        );
    }

    private void logException(Throwable ex, ErrorResponse errorResponse) {
        if (errorResponse.getStatus() >= 500) {
            log.error("서버 에러 [{}]: {}", errorResponse.getCode(), errorResponse.getMessage(), ex);
        } else {
            log.warn("클라이언트 에러 [{}]: {}", errorResponse.getCode(), errorResponse.getMessage());
        }
    }
}
