package snust.sbsp.common.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import snust.sbsp.common.exception.CustomCommonException;

public class Response<T> {

  public static <T> ResponseEntity<T> ok(HttpStatus httpStatus) {
    return new ResponseEntity<>(httpStatus);
  }

  public static <T> ResponseEntity<T> ok(HttpStatus httpStatus, T data) {
    return ResponseEntity
      .status(httpStatus)
      .body(data);
  }

  public static <T> ResponseEntity<T> ok(HttpStatus httpStatus, T data, ResponseCookie responseCookie) {
    return ResponseEntity
      .status(httpStatus)
      .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
      .body(data);
  }

  public static ResponseEntity<ErrorResponse> fail(CustomCommonException e) {
    return ResponseEntity
      .status(e.getHttpStatus())
      .body(new ErrorResponse(e));
  }

  @Getter
  @AllArgsConstructor
  static class ErrorResponse {
    private final int code;
    private final String message;

    ErrorResponse(CustomCommonException e) {
      this.code = e.getCode();
      this.message = e.getMessage();
    }
  }
}
