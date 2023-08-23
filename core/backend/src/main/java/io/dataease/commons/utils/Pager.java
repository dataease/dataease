package io.dataease.commons.utils;


import io.swagger.annotations.ApiModelProperty;

public class Pager<T> {
    @ApiModelProperty("数据")
    private T listObject;
    @ApiModelProperty("总数")
    private long itemCount;
    @ApiModelProperty("页数")
    private long pageCount;

    public Pager() {
    }

    public Pager(T listObject, long itemCount, long pageCount) {
        this.listObject = listObject;
        this.itemCount = itemCount;
        this.pageCount = pageCount;
    }

    public long getPageCount() {
        return pageCount;
    }

    public void setPageCount(long pageCount) {
        this.pageCount = pageCount;
    }


    public long getItemCount() {
        return itemCount;
    }

    public void setItemCount(long itemCount) {
        this.itemCount = itemCount;
    }

    public T getListObject() {
        return listObject;
    }

    public void setListObject(T listObject) {
        this.listObject = listObject;
    }
}
