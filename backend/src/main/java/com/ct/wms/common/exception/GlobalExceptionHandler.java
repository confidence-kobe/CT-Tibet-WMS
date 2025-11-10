package com.ct.wms.common.exception;

import com.ct.wms.common.api.Result;
import com.ct.wms.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * Global exception handler
 * Provides centralized exception handling for all controllers
 *
 * @author CT-Tibet-WMS
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle BusinessException
     *
     * @param e the business exception
     * @return Result with error information
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("Business exception occurred: code={}, message={}", e.getErrorCode(), e.getMessage(), e);
        return Result.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * Handle MethodArgumentNotValidException (Bean Validation for @RequestBody)
     *
     * @param e the method argument not valid exception
     * @return Result with validation error information
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.error("Validation exception occurred: {}", errorMessage, e);
        return Result.error(ResultCode.BAD_REQUEST, errorMessage);
    }

    /**
     * Handle BindException (Bean Validation for @ModelAttribute)
     *
     * @param e the bind exception
     * @return Result with validation error information
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("Bind exception occurred: {}", errorMessage, e);
        return Result.error(ResultCode.BAD_REQUEST, errorMessage);
    }

    /**
     * Handle ConstraintViolationException (Bean Validation for @RequestParam and @PathVariable)
     *
     * @param e the constraint violation exception
     * @return Result with validation error information
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.error("Constraint violation exception occurred: {}", errorMessage, e);
        return Result.error(ResultCode.BAD_REQUEST, errorMessage);
    }

    /**
     * Handle MethodArgumentTypeMismatchException
     *
     * @param e the method argument type mismatch exception
     * @return Result with error information
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String errorMessage = String.format("Parameter '%s' has invalid type. Expected type is %s",
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");
        log.error("Method argument type mismatch exception occurred: {}", errorMessage, e);
        return Result.error(ResultCode.BAD_REQUEST, errorMessage);
    }

    /**
     * Handle AccessDeniedException
     *
     * @param e the access denied exception
     * @return Result with error information
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied exception occurred: {}", e.getMessage(), e);
        return Result.error(ResultCode.FORBIDDEN, "Access denied");
    }

    /**
     * Handle IllegalArgumentException
     *
     * @param e the illegal argument exception
     * @return Result with error information
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument exception occurred: {}", e.getMessage(), e);
        return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * Handle IllegalStateException
     *
     * @param e the illegal state exception
     * @return Result with error information
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleIllegalStateException(IllegalStateException e) {
        log.error("Illegal state exception occurred: {}", e.getMessage(), e);
        return Result.error(ResultCode.INTERNAL_ERROR, e.getMessage());
    }

    /**
     * Handle NullPointerException
     *
     * @param e the null pointer exception
     * @return Result with error information
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("Null pointer exception occurred", e);
        return Result.error(ResultCode.INTERNAL_ERROR, "Internal server error: null pointer exception");
    }

    /**
     * Handle RuntimeException
     *
     * @param e the runtime exception
     * @return Result with error information
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);
        return Result.error(ResultCode.INTERNAL_ERROR, "Internal server error: " + e.getMessage());
    }

    /**
     * Handle all other exceptions
     *
     * @param e the exception
     * @return Result with error information
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        return Result.error(ResultCode.INTERNAL_ERROR, "An unexpected error occurred");
    }
}
