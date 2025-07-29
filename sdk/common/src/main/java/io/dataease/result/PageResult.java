package io.dataease.result;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 自定义分页结果类
 *
 * @author jianneng
 * @date 2025/6/6 10:36
 **/
@Getter
@Setter
@NoArgsConstructor
public class PageResult<T> {
    /**
     * 数据
     */
    private List<T> records;
    /**
     * 总数
     */
    private long total;
    /**
     * 当前页码
     */
    private long current;
    /**
     * 总页数
     */
    private long pages;
    /**
     * 每页大小
     */
    private long size;
    /**
     * 原始分页对象
     */
    private Page originalPage;

    /**
     * 自定义分页结果类
     *
     * @param page             分页对象 JPA原始分页对象
     * @param showOriginalPage 是否显示原始分页对象
     */
    public PageResult(Page page, boolean... showOriginalPage) {
        this.records = page.getContent();
        this.total = page.getTotalElements();
        this.current = page.getNumber() + 1;
        this.pages = page.getPageable().getPageNumber();
        this.size = page.getSize();
        if (showOriginalPage.length > 0 && showOriginalPage[0]) {
            this.originalPage = page;
        }
    }

    /**
     * 自定义分页结果类
     *
     * @param list  数据列表
     * @param total 总数
     * @param page  分页对象 分页信息
     */
    public PageResult(List<T> list, Long total, Pageable page) {
        this.records = list;
        this.total = total;
        this.current = page.getPageNumber() + 1;
        this.pages = page.getPageNumber();
        this.size = page.getPageSize();
    }
}
