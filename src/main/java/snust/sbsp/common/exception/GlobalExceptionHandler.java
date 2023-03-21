package snust.sbsp.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import snust.sbsp.common.res.Response;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomCommonException.class)
    public ResponseEntity<?> customCommonException(CustomCommonException e) {
        return Response.fail(e);
    }
}
