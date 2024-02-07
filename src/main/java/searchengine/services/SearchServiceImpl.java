package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.BaseResponse;
import searchengine.dto.search.SearchInfo;
import searchengine.dto.search.SearchOptions;
import searchengine.dto.search.SearchResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SiteService siteService;

    @Override
    public BaseResponse getSearchResult(SearchOptions searchOptions) {
        SearchResponse searchResponse = new SearchResponse();
        List<SearchInfo> data = searchResponse.getData();
        List<SiteEntity> sites = getSites(searchOptions.site());
        if (sites.isEmpty()) {
            throw new RuntimeException();
        }

        for (SiteEntity site : sites) {
            SearchInfo searchInfo = new SearchInfo();
            searchInfo.setSite(site.getUrl());
            searchInfo.setSiteName(site.getName());
            List<LemmaEntity> lemmas = getLemmas(searchOptions.query(), site.getId());
            List<PageEntity> pages = getPages(lemmas);
            for (PageEntity page : pages) {
                searchInfo.setUri(page.getPath());
            }
            data.add(searchInfo);
        }
        searchResponse.setCount(data.size());
        return searchResponse;
    }

    private List<SiteEntity> getSites(String site) {
        List<SiteEntity> sites = new ArrayList<>();
        if (site == null) {
            Iterable<SiteEntity> siteEntities = siteService.findAllSites();
            for (SiteEntity siteEntity : siteEntities) {
                sites.add(siteEntity);
            }
        } else {
            Optional<SiteEntity> maybeSite = siteService.findSiteByUrl(site);
            maybeSite.ifPresent(sites::add);
        }
        return sites;
    }

    private List<LemmaEntity> getLemmas(String query, Integer id) {
        return null;
    }

    private List<PageEntity> getPages(List<LemmaEntity> lemmas) {
        return null;
    }
}
