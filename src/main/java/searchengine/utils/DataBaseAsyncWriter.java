package searchengine.utils;

import searchengine.model.SiteEntity;
import searchengine.services.SiteService;

public class DataBaseAsyncWriter implements Runnable {
    private static final int BATCH_SIZE_FOR_SAVE_PAGE = 15;
    private final PageWalker siteIndexer;
    private final SiteEntity site;
    private final SiteService siteService;

    public DataBaseAsyncWriter(PageWalker siteIndexer, SiteEntity site, SiteService siteService) {
        this.siteIndexer = siteIndexer;
        this.site = site;
        this.siteService = siteService;
    }

    @Override
    public void run() {
        int index = 0;
        while (siteIndexer.getRunning()) {
            if (site.getPagesSize() >= index + BATCH_SIZE_FOR_SAVE_PAGE) {
                siteService.saveAllPages(site.getPages(index, index + BATCH_SIZE_FOR_SAVE_PAGE));
                index += BATCH_SIZE_FOR_SAVE_PAGE;
            }
        }

        int pagesSize = site.getPagesSize();
        if (pagesSize > index) {
            siteService.saveAllPages(site.getPages(index, pagesSize));
        }
    }
}
