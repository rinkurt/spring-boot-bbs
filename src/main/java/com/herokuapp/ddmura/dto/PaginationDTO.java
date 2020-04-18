package com.herokuapp.ddmura.dto;

import lombok.Data;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaginationDTO <T> {
    private List<T> list;
    private boolean showPrevious;
    private boolean showFirstPage;
    private boolean showNext;
    private boolean showEndPage;
    private Integer page;
    private List<Integer> pages = new ArrayList<>();
    private Integer totalPage;

    private int offset;
    private int size;

    public RowBounds setPagination(Integer totalCount, Integer page, Integer size) {
        int totalPage;
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        }
        else {
            totalPage = totalCount / size + 1;
        }
        if (totalPage == 0) {
            totalPage = 1;
        }
        this.totalPage = totalPage;

        if (page < 1) page = 1;
        if (page > totalPage) page = totalPage;
        this.page = page;

        offset = size * (page - 1);
        this.size = size;

        for (int i = -3; i <= 3; i++) {
            int curPage = page + i;
            if (curPage > 0 && curPage <= totalPage) {
                pages.add(curPage);
            }
        }

        showPrevious = !page.equals(1);
        showNext = !page.equals(totalPage);

        showFirstPage = !pages.contains(1);
        showEndPage = !pages.contains(totalPage);

        return new RowBounds(this.offset, this.size);
    }
}
