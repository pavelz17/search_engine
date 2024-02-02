package searchengine.services;

import searchengine.dto.BaseResponse;
import searchengine.dto.model.PageDto;
import searchengine.model.SiteEntity;

public interface IndexingSiteService {
    BaseResponse startIndexing();
    BaseResponse stopIndexing();
    BaseResponse pageIndexing(String url);

    void createIndex(SiteEntity site, String url, PageDto pageDto);

    boolean getIndexing();
}
