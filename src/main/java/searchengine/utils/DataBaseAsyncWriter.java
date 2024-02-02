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

    }
}
