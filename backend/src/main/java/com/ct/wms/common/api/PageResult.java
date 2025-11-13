package com.ct.wms.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Paginated response wrapper
 * Extends Result to provide pagination information along with data
 *
 * @param <T> the type of data in the page
 * @author CT-Tibet-WMS
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<List<T>> {

    private static final long serialVersionUID = 1L;

    /**
     * Total number of records
     */
    private Long total;

    /**
     * Current page number
     */
    private Integer pageNum;

    /**
     * Page size (records per page)
     */
    private Integer pageSize;

    /**
     * Total number of pages
     */
    private Integer pages;

    /**
     * Create a successful paginated response
     *
     * @param data the list of data for current page
     * @param total total number of records
     * @param pageNum current page number
     * @param pageSize page size
     * @param <T> the type of data
     * @return PageResult object with success status
     */
    public static <T> PageResult<T> success(List<T> data, Long total, Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(calculatePages(total, pageSize));
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * Create a successful paginated response with custom message
     *
     * @param data the list of data for current page
     * @param total total number of records
     * @param pageNum current page number
     * @param pageSize page size
     * @param message custom success message
     * @param <T> the type of data
     * @return PageResult object with success status
     */
    public static <T> PageResult<T> success(List<T> data, Long total, Integer pageNum, Integer pageSize, String message) {
        PageResult<T> result = new PageResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(calculatePages(total, pageSize));
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * Create a successful paginated response from MyBatis-Plus Page object
     *
     * @param page MyBatis-Plus Page object
     * @param <T> the type of data
     * @return PageResult object with success status
     */
    public static <T> PageResult<T> of(Page<T> page) {
        return success(page.getRecords(), page.getTotal(),
                      (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * Create an empty paginated response
     *
     * @param pageNum current page number
     * @param pageSize page size
     * @param <T> the type of data
     * @return PageResult object with empty data
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return success(List.of(), 0L, pageNum, pageSize);
    }


    /**
     * Calculate total number of pages
     *
     * @param total total number of records
     * @param pageSize page size
     * @return total number of pages
     */
    private static Integer calculatePages(Long total, Integer pageSize) {
        if (total == null || total == 0 || pageSize == null || pageSize == 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }
}
