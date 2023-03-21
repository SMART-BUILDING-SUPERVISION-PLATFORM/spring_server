package snust.sbsp.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomCommonException extends RuntimeException {

    private final int code;
    private final HttpStatus httpStatus;

    public CustomCommonException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
