package root.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class Advice {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handle(NoAuthenticationKey e) {}
}
