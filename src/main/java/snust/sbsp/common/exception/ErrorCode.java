package snust.sbsp.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // 400 잘못된 요청
    PENDING_LOGIN(-401, HttpStatus.BAD_REQUEST, "you are in pending state."),
    PASSWORD_INVALID(-402, HttpStatus.BAD_REQUEST, "password invalid."),
    COMPANY_HAS_ADMIN(-403, HttpStatus.BAD_REQUEST, "company you've been selected has admin."),
    EMAIL_DUPLICATED(-404, HttpStatus.BAD_REQUEST, "email duplicated."),
    EMAIL_CODE_INVALID(-405, HttpStatus.BAD_REQUEST, "email code invalid."),

    // 403 FORBIDDEN 접근 실패

    // 404 NON_FOUND 존재하지 않음
    COMPANY_NOT_FOUND(-406, HttpStatus.NOT_FOUND, "company not found."),
    EMAIL_CODE_NOT_FOUND(-407, HttpStatus.NOT_FOUND, "mail code not found."),
    CREW_NOT_FOUND(-408, HttpStatus.NOT_FOUND, "crew not found."),


    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(-501, HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
