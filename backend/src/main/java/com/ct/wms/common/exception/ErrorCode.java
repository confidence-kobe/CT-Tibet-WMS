package com.ct.wms.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Error code enumeration
 * Defines business error codes with corresponding messages
 *
 * @author CT-Tibet-WMS
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // User related errors (1001-1099)
    /**
     * User not found
     */
    USER_NOT_FOUND(1001, "User not found"),

    /**
     * User already exists
     */
    USER_ALREADY_EXISTS(1002, "User already exists"),

    /**
     * Invalid username or password
     */
    INVALID_CREDENTIALS(1003, "Invalid username or password"),

    /**
     * User account is disabled
     */
    USER_DISABLED(1004, "User account is disabled"),

    /**
     * User account is locked
     */
    USER_LOCKED(1005, "User account is locked"),

    /**
     * Password does not match
     */
    PASSWORD_MISMATCH(1006, "Password does not match"),

    /**
     * Old password is incorrect
     */
    OLD_PASSWORD_INCORRECT(1007, "Old password is incorrect"),

    // Material related errors (1101-1199)
    /**
     * Material not found
     */
    MATERIAL_NOT_FOUND(1101, "Material not found"),

    /**
     * Material already exists
     */
    MATERIAL_ALREADY_EXISTS(1102, "Material already exists"),

    /**
     * Material code is duplicated
     */
    MATERIAL_CODE_DUPLICATED(1103, "Material code is duplicated"),

    /**
     * Material is in use and cannot be deleted
     */
    MATERIAL_IN_USE(1104, "Material is in use and cannot be deleted"),

    // Inventory related errors (1201-1299)
    /**
     * Insufficient inventory
     */
    INSUFFICIENT_INVENTORY(1201, "Insufficient inventory"),

    /**
     * Inventory not found
     */
    INVENTORY_NOT_FOUND(1202, "Inventory not found"),

    /**
     * Inventory already exists
     */
    INVENTORY_ALREADY_EXISTS(1203, "Inventory already exists"),

    /**
     * Invalid inventory quantity
     */
    INVALID_INVENTORY_QUANTITY(1204, "Invalid inventory quantity"),

    /**
     * Inventory is locked
     */
    INVENTORY_LOCKED(1205, "Inventory is locked"),

    // Warehouse related errors (1301-1399)
    /**
     * Warehouse not found
     */
    WAREHOUSE_NOT_FOUND(1301, "Warehouse not found"),

    /**
     * Warehouse already exists
     */
    WAREHOUSE_ALREADY_EXISTS(1302, "Warehouse already exists"),

    /**
     * Warehouse code is duplicated
     */
    WAREHOUSE_CODE_DUPLICATED(1303, "Warehouse code is duplicated"),

    /**
     * Warehouse is in use and cannot be deleted
     */
    WAREHOUSE_IN_USE(1304, "Warehouse is in use and cannot be deleted"),

    /**
     * Warehouse capacity exceeded
     */
    WAREHOUSE_CAPACITY_EXCEEDED(1305, "Warehouse capacity exceeded"),

    // Location related errors (1401-1499)
    /**
     * Location not found
     */
    LOCATION_NOT_FOUND(1401, "Location not found"),

    /**
     * Location already exists
     */
    LOCATION_ALREADY_EXISTS(1402, "Location already exists"),

    /**
     * Location code is duplicated
     */
    LOCATION_CODE_DUPLICATED(1403, "Location code is duplicated"),

    /**
     * Location is occupied
     */
    LOCATION_OCCUPIED(1404, "Location is occupied"),

    /**
     * Location is disabled
     */
    LOCATION_DISABLED(1405, "Location is disabled"),

    // Order related errors (1501-1599)
    /**
     * Order not found
     */
    ORDER_NOT_FOUND(1501, "Order not found"),

    /**
     * Order already exists
     */
    ORDER_ALREADY_EXISTS(1502, "Order already exists"),

    /**
     * Order status is invalid
     */
    INVALID_ORDER_STATUS(1503, "Order status is invalid"),

    /**
     * Order cannot be modified
     */
    ORDER_CANNOT_BE_MODIFIED(1504, "Order cannot be modified"),

    /**
     * Order cannot be cancelled
     */
    ORDER_CANNOT_BE_CANCELLED(1505, "Order cannot be cancelled"),

    /**
     * Order is already completed
     */
    ORDER_ALREADY_COMPLETED(1506, "Order is already completed"),

    // Supplier related errors (1601-1699)
    /**
     * Supplier not found
     */
    SUPPLIER_NOT_FOUND(1601, "Supplier not found"),

    /**
     * Supplier already exists
     */
    SUPPLIER_ALREADY_EXISTS(1602, "Supplier already exists"),

    /**
     * Supplier code is duplicated
     */
    SUPPLIER_CODE_DUPLICATED(1603, "Supplier code is duplicated"),

    /**
     * Supplier is in use and cannot be deleted
     */
    SUPPLIER_IN_USE(1604, "Supplier is in use and cannot be deleted"),

    // Customer related errors (1701-1799)
    /**
     * Customer not found
     */
    CUSTOMER_NOT_FOUND(1701, "Customer not found"),

    /**
     * Customer already exists
     */
    CUSTOMER_ALREADY_EXISTS(1702, "Customer already exists"),

    /**
     * Customer code is duplicated
     */
    CUSTOMER_CODE_DUPLICATED(1703, "Customer code is duplicated"),

    /**
     * Customer is in use and cannot be deleted
     */
    CUSTOMER_IN_USE(1704, "Customer is in use and cannot be deleted"),

    // Permission related errors (1801-1899)
    /**
     * Permission denied
     */
    PERMISSION_DENIED(1801, "Permission denied"),

    /**
     * Role not found
     */
    ROLE_NOT_FOUND(1802, "Role not found"),

    /**
     * Role already exists
     */
    ROLE_ALREADY_EXISTS(1803, "Role already exists"),

    /**
     * Role is in use and cannot be deleted
     */
    ROLE_IN_USE(1804, "Role is in use and cannot be deleted"),

    // Validation errors (1901-1999)
    /**
     * Invalid parameter
     */
    INVALID_PARAMETER(1901, "Invalid parameter"),

    /**
     * Parameter cannot be null
     */
    PARAMETER_CANNOT_BE_NULL(1902, "Parameter cannot be null"),

    /**
     * Invalid date format
     */
    INVALID_DATE_FORMAT(1903, "Invalid date format"),

    /**
     * Invalid file format
     */
    INVALID_FILE_FORMAT(1904, "Invalid file format"),

    /**
     * File size exceeds limit
     */
    FILE_SIZE_EXCEEDED(1905, "File size exceeds limit"),

    /**
     * Data validation failed
     */
    VALIDATION_FAILED(1906, "Data validation failed"),

    // System errors (2001-2099)
    /**
     * Database operation failed
     */
    DATABASE_ERROR(2001, "Database operation failed"),

    /**
     * Network error
     */
    NETWORK_ERROR(2002, "Network error"),

    /**
     * External service error
     */
    EXTERNAL_SERVICE_ERROR(2003, "External service error"),

    /**
     * System busy, please try again later
     */
    SYSTEM_BUSY(2004, "System busy, please try again later"),

    /**
     * Configuration error
     */
    CONFIGURATION_ERROR(2005, "Configuration error"),

    /**
     * Unknown error
     */
    UNKNOWN_ERROR(9999, "Unknown error");

    /**
     * Error code
     */
    private final Integer code;

    /**
     * Error message
     */
    private final String message;
}
