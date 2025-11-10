package com.ct.wms.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration for API response codes
 * Defines standard HTTP status codes and custom business error codes
 *
 * @author CT-Tibet-WMS
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * Success - 200
     */
    SUCCESS(200, "Success"),

    /**
     * Bad Request - 400
     */
    BAD_REQUEST(400, "Bad Request"),

    /**
     * Unauthorized - 401
     */
    UNAUTHORIZED(401, "Unauthorized"),

    /**
     * Forbidden - 403
     */
    FORBIDDEN(403, "Forbidden"),

    /**
     * Not Found - 404
     */
    NOT_FOUND(404, "Not Found"),

    /**
     * Internal Server Error - 500
     */
    INTERNAL_ERROR(500, "Internal Server Error"),

    /**
     * Business Error - 1000
     */
    BUSINESS_ERROR(1000, "Business Error");

    /**
     * Response code
     */
    private final Integer code;

    /**
     * Response message
     */
    private final String message;
}
