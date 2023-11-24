package com.deweydatasystem.exceptions;

import com.deweydatasystem.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import com.deweydatasystem.exceptions.*;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@ControllerAdvice
@Slf4j
public class ExceptionMapper extends ResponseEntityExceptionHandler {

    private final String MESSAGE_KEY = "message";

    private final String LOG_MESSAGE_400 = "An exception returning a 400 response occurred \n";

    private final String LOG_MESSAGE_500 = "An exception returning a 500 response occurred \n";

    @ExceptionHandler({
            CriterionColumnDataTypeAndFilterMismatchException.class,
            UncleanSqlException.class,
            DatabaseTypeNotRecognizedException.class,
            SqlTypeNotRecognizedException.class,
            SqlTypeNotRecognizedException.class,
            UncleanSqlException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<?> handleRuntimeExceptionsThatShouldReturn400Response(RuntimeException ex) {
        MDC.put(Constants.RESPONSE_CODE, "400");
        log.error(LOG_MESSAGE_400, ex);

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        Map.of(
                                MESSAGE_KEY,
                                ex.getMessage() == null ? "" : ex.getMessage()
                        )
                );
    }

    @ExceptionHandler({
            CacheMissException.class,
            QueryTemplateNotFoundException.class
    })
    public ResponseEntity<?> handleRuntimeExceptionsThatShouldReturn404Responses(RuntimeException ex) {
        MDC.put(Constants.RESPONSE_CODE, "404");
        log.error("An exception returning a 404 response occurred ", ex);

        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({
            QueryFailureException.class,
            IOException.class,
            TimeoutException.class
    })
    public ResponseEntity<?> handleExceptionsThatShouldReturn500Response(Exception ex) {
        MDC.put(Constants.RESPONSE_CODE, "500");
        log.error(LOG_MESSAGE_500, ex);

        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        Map.of(
                                "message",
                                ex.getMessage() == null ? "" : ex.getMessage()
                        )
                );
    }

    @ExceptionHandler({
            JsonDeserializationException.class,
            JsonSerializationException.class,
            CacheRefreshException.class
    })
    public ResponseEntity<?> handleRuntimeExceptionsThatShouldReturn500Response(RuntimeException ex) {
        MDC.put(Constants.RESPONSE_CODE, "500");
        log.error(LOG_MESSAGE_500, ex);

        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        Map.of(
                                "message",
                                ex.getMessage() == null ? "" : ex.getMessage()
                        )
                );
    }

    /**
     * Override's Spring's default behavior when a request's body cannot be deserialized.
     *
     * @param ex {@link HttpMessageNotReadableException}
     * @param headers {@link HttpHeaders}
     * @param status {@link HttpStatus}
     * @param request {@link WebRequest}
     * @return {@link ResponseEntity<Object>}
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request
    ) {
        log.error(LOG_MESSAGE_400, ex);

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        Map.of(
                                "message",
                                ex.getMessage() == null ? "" : ex.getMessage()
                        )
                );
    }
}
