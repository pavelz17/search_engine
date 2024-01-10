package searchengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import searchengine.dto.ErrorResponse;

@ControllerAdvice
public class DefaultAdvice {
    @ExceptionHandler(IncorrectMethodCallException.class)
    public ResponseEntity<ErrorResponse> handleIndexingException(IncorrectMethodCallException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        response.setResult(false);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }
}
