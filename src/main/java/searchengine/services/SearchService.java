package searchengine.services;

import searchengine.dto.BaseResponse;
import searchengine.dto.search.SearchOptions;

public interface SearchService {
    BaseResponse getSearchResult(SearchOptions searchOptions);
}
