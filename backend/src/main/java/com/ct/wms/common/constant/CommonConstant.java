package com.ct.wms.common.constant;

/**
 * Common constants for the application
 * Contains frequently used constant values across the system
 *
 * @author CT-Tibet-WMS
 * @since 1.0
 */
public class CommonConstant {

    /**
     * JWT token prefix
     */
    public static final String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * Token header name
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Default page size for pagination
     */
    public static final Integer DEFAULT_PAGE_SIZE = 20;

    /**
     * Maximum page size for pagination
     */
    public static final Integer MAX_PAGE_SIZE = 100;

    /**
     * Default page number (first page)
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    /**
     * Success status code
     */
    public static final String SUCCESS_CODE = "200";

    /**
     * Error status code
     */
    public static final String ERROR_CODE = "500";

    /**
     * Default password for new users
     */
    public static final String DEFAULT_PASSWORD = "123456";

    /**
     * System admin role code
     */
    public static final String ROLE_ADMIN = "admin";

    /**
     * System user role code
     */
    public static final String ROLE_USER = "user";

    /**
     * Active status
     */
    public static final Integer STATUS_ACTIVE = 1;

    /**
     * Inactive status
     */
    public static final Integer STATUS_INACTIVE = 0;

    /**
     * Deleted flag - not deleted
     */
    public static final Integer DELETED_NO = 0;

    /**
     * Deleted flag - deleted
     */
    public static final Integer DELETED_YES = 1;

    /**
     * Character encoding - UTF-8
     */
    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * Date format pattern
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * DateTime format pattern
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Time format pattern
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * Request ID header
     */
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * User ID header
     */
    public static final String USER_ID_HEADER = "X-User-ID";

    /**
     * Tenant ID header
     */
    public static final String TENANT_ID_HEADER = "X-Tenant-ID";

    /**
     * Cache key prefix for user
     */
    public static final String CACHE_KEY_USER = "user:";

    /**
     * Cache key prefix for token
     */
    public static final String CACHE_KEY_TOKEN = "token:";

    /**
     * Cache key prefix for permission
     */
    public static final String CACHE_KEY_PERMISSION = "permission:";

    /**
     * Default cache expiration time (seconds) - 30 minutes
     */
    public static final Long CACHE_EXPIRATION_DEFAULT = 1800L;

    /**
     * Token expiration time (seconds) - 24 hours
     */
    public static final Long TOKEN_EXPIRATION = 86400L;

    /**
     * Refresh token expiration time (seconds) - 7 days
     */
    public static final Long REFRESH_TOKEN_EXPIRATION = 604800L;

    /**
     * File upload max size (bytes) - 10MB
     */
    public static final Long FILE_UPLOAD_MAX_SIZE = 10485760L;

    /**
     * Allowed image file extensions
     */
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    /**
     * Allowed document file extensions
     */
    public static final String[] ALLOWED_DOCUMENT_EXTENSIONS = {".pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt"};

    /**
     * Operation success message
     */
    public static final String OPERATION_SUCCESS = "Operation completed successfully";

    /**
     * Operation failed message
     */
    public static final String OPERATION_FAILED = "Operation failed";

    /**
     * Private constructor to prevent instantiation
     */
    private CommonConstant() {
        throw new IllegalStateException("Constant class");
    }
}
