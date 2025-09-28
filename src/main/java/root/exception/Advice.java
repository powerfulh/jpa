package root.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import root.plm.PlmException;

import java.util.Map;

@RestControllerAdvice
public class Advice {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handle(NoAuthenticationKey e) {}
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handle(PlmException e) {
        return e.info;
    }
}
