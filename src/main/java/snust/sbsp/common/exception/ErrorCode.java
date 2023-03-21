package snust.sbsp.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // 400 잘못된 요청
    PENDING_LOGIN(400, HttpStatus.BAD_REQUEST, "you are in pending state."),
    PASSWORD_INVALID(400, HttpStatus.BAD_REQUEST, "password invalid."),
    COMPANY_HAS_ADMIN(400, HttpStatus.BAD_REQUEST, "company you've been selected has admin."),
    EMAIL_DUPLICATED(400, HttpStatus.BAD_REQUEST, "email duplicated."),
    EMAIL_CODE_INVALID(400, HttpStatus.BAD_REQUEST, "email code invalid."),

    // 403 FORBIDDEN 접근 실패

    // 404 NON_FOUND 존재하지 않음
    COMPANY_NOT_FOUND(404, HttpStatus.NOT_FOUND, "company not found."),
    EMAIL_CODE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "mail code not found."),


    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다."),
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "user not found.");

    private final int status;
    private final HttpStatus httpStatus;
    private final String message;
}
