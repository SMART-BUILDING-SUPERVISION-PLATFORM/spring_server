package snust.sbsp.common.res;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import snust.sbsp.common.dto.ErrorResponse;

public class Response<T> {

  public static <T> ResponseEntity<T> ok(int status) {
    return new ResponseEntity<>(HttpStatus.valueOf(status));
  }

  public static <T> ResponseEntity<T> ok(int status, T data) {
    return ResponseEntity
      .status(HttpStatus.valueOf(status))
      .body(data);
  }

  public static <T> ResponseEntity<T> ok(int status, T data, ResponseCookie responseCookie) {
    return ResponseEntity
      .status(HttpStatus.valueOf(status))
      .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
      .body(data);
  }

  public static ResponseEntity<ErrorResponse> fail(int errStatus, String errorMessage) {
    return ResponseEntity
      .status(HttpStatus.valueOf(errStatus))
      .body(ErrorResponse.builder()
        .status(errStatus)
        .errorMessage(errorMessage)
        .build());
  }
}
