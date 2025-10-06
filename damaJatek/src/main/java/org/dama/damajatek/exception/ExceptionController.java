package org.dama.damajatek.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.persistence.EntityNotFoundException;
import org.dama.damajatek.dto.CustomExceptionDto;
import org.dama.damajatek.exception.auth.UserNotLoggedInException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class ExceptionController {

    // ----------------------
    // Validation Exceptions
    // ----------------------
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<CustomExceptionDto> handleMethodValidationException(
            HandlerMethodValidationException e
    ) {
        String errorMessage = e.getAllErrors().stream()
                .findFirst()
                .map(MessageSourceResolvable::getDefaultMessage)
                .orElse("Validation failed");

        return buildResponse(errorMessage, BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomExceptionDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        List<String> globalErrors = ex.getBindingResult()
                .getGlobalErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        List<String> allErrors = new ArrayList<>();
        allErrors.addAll(globalErrors);
        allErrors.addAll(fieldErrors);

        String combinedMessage = String.join(", ", allErrors);
        return buildResponse(combinedMessage.isEmpty() ? "Validation failed" : combinedMessage, BAD_REQUEST);
    }

    // ----------------------
    // CompletionException delegation
    // ----------------------
    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<CustomExceptionDto> handleCompletionException(
            CompletionException e
    ) {
        Throwable cause = e.getCause();
        if (cause instanceof UserNotFoundException) {
            return handleUserNotFoundException((UserNotFoundException) cause);
        } else if (cause instanceof IllegalStateException) {
            return handleIllegalStateException((IllegalStateException) cause);
        }
        return globalExceptionHandler(e);
    }

    // ----------------------
    // Specific exceptions
    // ----------------------
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleUserNotFoundException(UserNotFoundException e) {
        return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CustomExceptionDto> handleIllegalStateException(IllegalStateException e) {
        return buildResponse(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<CustomExceptionDto> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        return buildResponse(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleEntityNotFoundException(EntityNotFoundException e) {
        return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CustomExceptionDto> handleJwtExpirationException(ExpiredJwtException e) {
        return buildResponse("Session expired.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<CustomExceptionDto> handleMalformedJwtException(MalformedJwtException e) {
        return buildResponse("Invalid JWT token.", BAD_REQUEST);
    }

    @ExceptionHandler(UserNotLoggedInException.class)
    public ResponseEntity<CustomExceptionDto> handleUserNotLoggedInException(UserNotLoggedInException e) {
        return buildResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CustomExceptionDto> handleBadCredentialsException(BadCredentialsException e) {
        return buildResponse(e.getMessage(), BAD_REQUEST);
    }

    // ----------------------
    // Fallback for other exceptions
    // ----------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomExceptionDto> globalExceptionHandler(Exception e) {
        return buildResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ----------------------
    // Utility method to build consistent responses
    // ----------------------
    private ResponseEntity<CustomExceptionDto> buildResponse(String message, HttpStatus status) {
        CustomExceptionDto response = CustomExceptionDto.builder()
                .date(new Date())
                .statusCode(status.value())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
