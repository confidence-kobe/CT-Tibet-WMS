package com.ct.wms.common.annotation;

import java.lang.annotation.*;

/**
 * Custom annotation for operation logging
 * Used to mark methods that require operation logging
 *
 * @author CT-Tibet-WMS
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * Operation title/description
     *
     * @return the operation title
     */
    String title() default "";

    /**
     * Business type
     *
     * @return the business type
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * Whether to save request parameters
     *
     * @return true if request parameters should be saved
     */
    boolean saveRequestData() default true;

    /**
     * Whether to save response data
     *
     * @return true if response data should be saved
     */
    boolean saveResponseData() default true;

    /**
     * Business type enumeration
     */
    enum BusinessType {
        /**
         * Other operation
         */
        OTHER("Other"),

        /**
         * Insert operation
         */
        INSERT("Insert"),

        /**
         * Update operation
         */
        UPDATE("Update"),

        /**
         * Delete operation
         */
        DELETE("Delete"),

        /**
         * Query operation
         */
        SELECT("Select"),

        /**
         * Export operation
         */
        EXPORT("Export"),

        /**
         * Import operation
         */
        IMPORT("Import"),

        /**
         * Login operation
         */
        LOGIN("Login"),

        /**
         * Logout operation
         */
        LOGOUT("Logout"),

        /**
         * Authorization operation
         */
        GRANT("Grant"),

        /**
         * Force logout
         */
        FORCE_LOGOUT("Force Logout"),

        /**
         * Clear cache
         */
        CLEAN_CACHE("Clean Cache");

        private final String description;

        BusinessType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
