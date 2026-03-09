package backend.academy.linktracker.scrapper.exception;

import backend.academy.linktracker.scrapper.dto.ApiErrorResponse;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ScrapperExceptionHandler {

    @ExceptionHandler({ChatNotFoundException.class, LinkNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex) {
        log.atWarn().setCause(ex).log("Ресурс не найден");
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Ресурс не найден");
    }

    @ExceptionHandler({ChatAlreadyExistsException.class, LinkAlreadyTrackedException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex) {
        log.atWarn().setCause(ex).log("Конфликт данных");
        return buildErrorResponse(ex, HttpStatus.CONFLICT, "Конфликт данных");
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        MissingRequestHeaderException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex) {
        log.atWarn().setCause(ex).log("Некорректные параметры запроса");
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Некорректные параметры запроса");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        log.atError().setCause(ex).log("Непредвиденная ошибка сервера");
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    @ExceptionHandler(InvalidLinkException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLink(InvalidLinkException ex) {
        log.atWarn().setCause(ex).log("Некорректный формат ссылки");
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Некорректные параметры запроса");
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(Exception ex, HttpStatus status, String description) {
        List<String> stacktrace = Arrays.stream(ex.getStackTrace())
            .map(StackTraceElement::toString)
            .toList();

        ApiErrorResponse response = new ApiErrorResponse(
            description,
            String.valueOf(status.value()),
            ex.getClass().getSimpleName(),
            ex.getMessage(),
            stacktrace
        );
        return new ResponseEntity<>(response, status);
    }
}
