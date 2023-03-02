package io.dataease.utils;

public class Pager<T> {
    private T listObject;
    private long itemCount;
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
