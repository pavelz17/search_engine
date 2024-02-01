package searchengine.services;

import searchengine.dto.BaseResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

public interface IndexingSiteService {
    BaseResponse startIndexing();
    BaseResponse stopIndexing();
    BaseResponse pageIndexing(String url);

    void createIndex(SiteEntity site, PageEntity page);

    boolean getIndexing();
}
