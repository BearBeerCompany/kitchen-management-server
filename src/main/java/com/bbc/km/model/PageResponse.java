package com.bbc.km.model;

import java.io.Serializable;
import java.util.List;

public class PageResponse<T> implements Serializable {
    private List<T> elements;
    private Integer page;
    private Integer size;
    private Integer totalPage;
    private Long totalSize;

    public PageResponse() {
    }

    public PageResponse(List<T> elements, Integer page, Integer size, Integer totalPage, Long totalSize) {
        this.elements = elements;
        this.page = page;
        this.size = size;
        this.totalPage = totalPage;
        this.totalSize = totalSize;
    }

    public static <T> PageResponse<T> of(List<T> data, Integer page, Integer size, Long total) {
        return new PageResponse<>(
                data,
                page,
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

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
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

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }
}
