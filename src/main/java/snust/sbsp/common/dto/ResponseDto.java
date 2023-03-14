package snust.sbsp.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
  private boolean ok;
  private T body;
  private Error error;

  public static <T> ResponseDto<T> success(T body) {
    return new ResponseDto<>(true, body, null);
  }

  public static <T> ResponseDto<T> fail(int status, HttpStatus httpStatus, String message) {
    return new ResponseDto<>(false, null, new Error(status, httpStatus, message));
  }

  @Getter
  @AllArgsConstructor
  static class Error {
    private final int status;
    private final HttpStatus httpStatus;
    private final String message;
  }
}
