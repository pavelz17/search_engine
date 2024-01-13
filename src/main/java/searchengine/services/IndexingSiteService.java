package searchengine.services;

import searchengine.dto.BaseResponse;

public interface IndexingSiteService {
    BaseResponse startIndexing();
    BaseResponse stopIndexing();
}
