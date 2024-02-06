package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.BaseResponse;
import searchengine.dto.search.SearchOptions;
import searchengine.dto.search.SearchResponse;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SiteService siteService;

    @Override
    public BaseResponse getSearchResult(SearchOptions searchOptions) {
        SearchResponse searchResponse = new SearchResponse();


        return searchResponse;
    }
}
