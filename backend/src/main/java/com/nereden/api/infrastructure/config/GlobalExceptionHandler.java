package com.nereden.api.infrastructure.config;

import com.nereden.api.application.dto.ApiErrorResponse;
import com.nereden.api.application.exception.EmailAlreadyExistsException;
import com.nereden.api.application.exception.InvalidCredentialsException;
import com.nereden.api.application.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        final String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity.badRequest().body(ApiErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.builder()
                .code("EMAIL_EXISTS")
                .message(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .build());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.builder()
                .code("INVALID_CREDENTIALS")
                .message(ex.getMessage())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .build());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.builder()
                .code("INVALID_TOKEN")
                .message(ex.getMessage())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.builder()
                .code("BAD_REQUEST")
                .message("Dosya gereklidir.")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponse> handleMultipart(MultipartException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.builder()
                .code("BAD_REQUEST")
                .message("Geçersiz dosya yüklemesi.")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.builder()
                .code("BAD_REQUEST")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.builder()
                .code("CONFLICT")
                .message(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .build());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleNotImplemented(UnsupportedOperationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiErrorResponse.builder()
                .code("NOT_IMPLEMENTED")
                .message(ex.getMessage())
                .statusCode(HttpStatus.NOT_IMPLEMENTED.value())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Beklenmeyen bir hata oluştu.")
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build());
    }
}
