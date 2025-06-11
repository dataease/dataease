package io.dataease.api.sync.commons;

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
    private List<T> records;
    private long total;
    private long current;
    private long pages;
    private long size;
    private Page originalPage;

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

    public PageResult(List<T> list, Long total, Pageable page) {
        this.records = list;
        this.total = total;
        this.current = page.getPageNumber() + 1;
        this.pages = page.getPageNumber();
        this.size = page.getPageSize();
    }
}
