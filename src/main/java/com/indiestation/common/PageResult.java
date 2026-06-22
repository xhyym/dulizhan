package com.indiestation.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 *
 * @param <T> 数据类型
 * @author IndieStation
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<T> records;

    /** 当前页码 */
    private long current;

    /** 每页条数 */
    private long size;

    /** 总条数 */
    private long total;

    /** 总页数 */
    private long pages;

    public PageResult() {}

    public PageResult(List<T> records, long current, long size, long total) {
        this.records = records;
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = (total + size - 1) / size;
    }
}
