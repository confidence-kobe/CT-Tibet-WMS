package com.ct.wms.common.exception;

import lombok.Getter;

/**
 * Custom business exception
 * Used to handle business logic errors throughout the application
 *
 * @author CT-Tibet-WMS
 * @since 1.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Error code
     */
    private final Integer errorCode;

    /**
     * Error message
     */
    private final String message;

    /**
     * Constructor with error code enum
     *
     * @param errorCode the error code enum
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * Constructor with error code enum and custom message
     *
     * @param errorCode the error code enum
     * @param message custom error message
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode.getCode();
        this.message = message;
    }

    /**
     * Constructor with custom code and message
     *
     * @param errorCode custom error code
     * @param message custom error message
     */
    public BusinessException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * Constructor with message only (uses default business error code)
     *
     * @param message error message
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = 1000;
        this.message = message;
    }

    /**
     * Constructor with error code enum and cause
     *
     * @param errorCode the error code enum
     * @param cause the cause of the exception
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * Constructor with error code enum, custom message, and cause
     *
     * @param errorCode the error code enum
     * @param message custom error message
     * @param cause the cause of the exception
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
        this.message = message;
    }
}
