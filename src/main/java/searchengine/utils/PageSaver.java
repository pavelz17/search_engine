package searchengine.utils;

import lombok.RequiredArgsConstructor;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
@RequiredArgsConstructor
public class PageSaver implements Runnable {
    private static final int BATCH_SIZE = 15;
    private final PageWalker walker;
    private final PageRepository pageRepository;

    @Override
    public void run() {
        SiteEntity siteEntity = walker.getSiteEntity();
        int index = 0;

        while (walker.getRunning()) {
            if(siteEntity.getPagesSize() >= index + BATCH_SIZE) {
                pageRepository.saveAll(siteEntity.getPages(index, index + BATCH_SIZE));
                index += BATCH_SIZE;
            }
        }

        int pagesSize = siteEntity.getPagesSize();
        if (pagesSize > index) {
            pageRepository.saveAll(siteEntity.getPages(index, pagesSize));
        }
    }
}
