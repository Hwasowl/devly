package se.sowl.devlybatch.job.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.Page;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.util.List;

@RequiredArgsConstructor
public class CompletedStudiesReader implements ItemReader<List<UserStudy>> {
    private final StudyService studyService;
    private final int pageSize;

    private int currentPage = 0;
    private boolean hasMoreData = true;

    @Override
    public List<UserStudy> read() {
        if (!hasMoreData) {
            return null;
        }

        Page<UserStudy> page = studyService.findCompletedStudiesWithoutNext(currentPage, pageSize);
        List<UserStudy> items = page.getContent();

        updatePagingState(items, page);

        return items.isEmpty() ? null : items;
    }

    private void updatePagingState(List<UserStudy> items, Page<UserStudy> page) {
        currentPage++;

        if (items.isEmpty() || currentPage >= page.getTotalPages()) {
            hasMoreData = false;
        }
    }
}
