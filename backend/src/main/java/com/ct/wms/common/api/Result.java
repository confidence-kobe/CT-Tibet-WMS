package com.ct.wms.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Unified API response wrapper
 * Provides a standardized response format for all API endpoints
 *
 * @param <T> the type of data in the response
 * @author CT-Tibet-WMS
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Response code
     */
    private Integer code;

    /**
     * Response message
     */
    private String message;

    /**
     * Response data
     */
    private T data;

    /**
     * Response timestamp
     */
    private LocalDateTime timestamp;

    /**
     * Create a successful response with data
     *
     * @param data the response data
     * @param <T> the type of data
     * @return Result object with success status
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * Create a successful response with custom message and data
     *
     * @param data the response data
     * @param message custom success message
     * @param <T> the type of data
     * @return Result object with success status
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * Create a successful response without data
     *
     * @param <T> the type of data
     * @return Result object with success status
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * Create a successful response with custom message
     *
     * @param message custom success message
     * @param <T> the type of data
     * @return Result object with success status
     */
    public static <T> Result<T> success(String message) {
        return success(null, message);
    }

    /**
     * Create an error response with result code
     *
     * @param resultCode the result code enum
     * @param <T> the type of data
     * @return Result object with error status
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * Create an error response with result code and custom message
     *
     * @param resultCode the result code enum
     * @param message custom error message
     * @param <T> the type of data
     * @return Result object with error status
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * Create an error response with custom code and message
     *
     * @param code error code
     * @param message error message
     * @param <T> the type of data
     * @return Result object with error status
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * Create a default error response
     *
     * @param <T> the type of data
     * @return Result object with error status
     */
    public static <T> Result<T> error() {
        return error(ResultCode.INTERNAL_ERROR);
    }

    /**
     * Create an error response with custom message
     *
     * @param message error message
     * @param <T> the type of data
     * @return Result object with error status
     */
    public static <T> Result<T> error(String message) {
        return error(ResultCode.INTERNAL_ERROR, message);
    }
}
