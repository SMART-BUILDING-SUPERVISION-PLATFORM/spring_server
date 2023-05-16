package snust.sbsp.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400 BAD_REQUEST 잘못된 요청 [-401, -410]
    PASSWORD_INVALID(-401, HttpStatus.BAD_REQUEST, "password invalid."),
    COMPANY_HAS_ADMIN(-402, HttpStatus.BAD_REQUEST, "company you've been selected has admin."),
    EMAIL_DUPLICATED(-403, HttpStatus.BAD_REQUEST, "email duplicated."),
    EMAIL_CODE_INVALID(-404, HttpStatus.BAD_REQUEST, "email code invalid."),
    BUSINESS_TYPE_INVALID(-405, HttpStatus.BAD_REQUEST, "business type invalid."),
    // 403 FORBIDDEN 접근 제한 [-411, -420]
    PENDING_STATE(-411, HttpStatus.FORBIDDEN, "you are in pending state."),
    FORBIDDEN(-412, HttpStatus.FORBIDDEN, "you don't have right to access."),
    // 404 NON_FOUND 존재하지 않음 [-421, -430]
    COMPANY_NOT_FOUND(-421, HttpStatus.NOT_FOUND, "company not found."),
    EMAIL_CODE_NOT_FOUND(-422, HttpStatus.NOT_FOUND, "mail code not found."),
    CREW_NOT_FOUND(-423, HttpStatus.NOT_FOUND, "crew not found."),
    SESSION_NOT_FOUND(-424, HttpStatus.NOT_FOUND, "session not found."),
    PROJECT_NOT_FOUND(-425, HttpStatus.NOT_FOUND, "project not found."),
    PARTICIPANT_NOT_FOUND(-426, HttpStatus.NOT_FOUND, "participant not found."),
    // 500 INTERNAL_SERVER_ERROR [-501, -510]
    INTERNAL_SERVER_ERROR(-501, HttpStatus.INTERNAL_SERVER_ERROR, "internal server error.");

    private final int code;

    private final HttpStatus httpStatus;

    private final String message;
}
