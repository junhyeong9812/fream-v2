package xyz.fream.api.global.presentation.exception;

/**
 * 전역 공토 예외
 * GlobalErrorCode를 사용하는 예외
 * */
public class CommonException extends GlobalException {
    /**
     * Error만으로 예외 발생
     * */
    public CommonException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * ErrorCode와 커스텀 메시지로 예외 생성
     * */
    public CommonException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * ErrorCode와 원인 예외로 예외 처리
     * */
    public CommonException(ErrorCode errorCode, Throwable cause) {
        super(errorCode,cause);
    }

    /**
     * ErrorCode, 커스텀 메시지, 원인 예외로 예외 생성
     * */
    public CommonException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
