package com.bbc.km.model;

import java.io.Serializable;
import java.util.List;

public class PageResponse<T> implements Serializable {
    private List<T> elements;
    private Integer offset;
    private Integer size;
    private Integer totalPage;
    private Integer totalSize;

    public PageResponse() {
    }

    public PageResponse(List<T> elements, Integer offset, Integer size, Integer totalPage, Integer totalSize) {
        this.elements = elements;
        this.offset = offset;
        this.size = size;
        this.totalPage = totalPage;
        this.totalSize = totalSize;
    }

    public static <T> PageResponse<T> of(List<T> data, Integer offset, Integer size, Integer total) {
        return new PageResponse<>(
                data,
                offset,
                data.size(),
                (int) Math.floor((double) total / size),
                total);
    }

    public List<T> getElements() {
        return elements;
    }

    public void setElements(List<T> elements) {
        this.elements = elements;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }
}
