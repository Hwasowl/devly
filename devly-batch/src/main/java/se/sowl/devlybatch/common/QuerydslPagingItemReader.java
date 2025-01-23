package se.sowl.devlybatch.common;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;

public class QuerydslPagingItemReader<T> implements ItemStreamReader<T> {
    private final Function<Pageable, Page<T>> queryFunction;
    private final int pageSize;
    private int currentItemIndex;
    private List<T> currentPage;
    private int pageNumber;
    private boolean noMoreData;

    public QuerydslPagingItemReader(
        Function<Pageable, Page<T>> queryFunction,
        int pageSize
    ) {
        this.queryFunction = queryFunction;
        this.pageSize = pageSize;
        this.currentItemIndex = 0;
        this.pageNumber = 0;
        this.noMoreData = false;
    }

    @Override
    public T read() {
        if (noMoreData) {
            return null;
        }

        if (currentPage == null || currentItemIndex >= currentPage.size()) {
            currentPage = fetchNextPage();
            currentItemIndex = 0;

            if (currentPage.isEmpty()) {
                noMoreData = true;
                return null;
            }
        }

        return currentPage.get(currentItemIndex++);
    }

    private List<T> fetchNextPage() {
        Page<T> page = queryFunction.apply(PageRequest.of(pageNumber++, pageSize));
        return page.getContent();
    }

    @Override
    public void open(ExecutionContext executionContext) {
        this.currentItemIndex = 0;
        this.pageNumber = 0;
        this.noMoreData = false;
        this.currentPage = null;
    }
}
