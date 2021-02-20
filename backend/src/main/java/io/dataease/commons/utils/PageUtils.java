package io.dataease.commons.utils;

import com.github.pagehelper.Page;

public class PageUtils {
    public static <T> Pager<T> setPageInfo(Page page, T obj) {
        try {
            Pager<T> pager = new Pager<>();
            pager.setListObject(obj);
            pager.setPageCount(page.getPages());
            pager.setItemCount(page.getTotal());
            return pager;
        } catch (Exception e) {
            throw new RuntimeException("Error saving current page number data！");
        }
    }
}
