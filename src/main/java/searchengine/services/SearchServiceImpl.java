package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.BaseResponse;
import searchengine.dto.search.SearchInfo;
import searchengine.dto.search.SearchOptions;
import searchengine.dto.search.SearchResponse;
import searchengine.exceptions.ErrorMessage;
import searchengine.exceptions.QueryParamError;
import searchengine.exceptions.SiteOutOfBoundConfigFile;
import searchengine.exceptions.SiteSearchStatusError;
import searchengine.model.*;
import searchengine.utils.LemmaFinder;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private static final int MAX_LEMMA_FREQUENCY = 50;
    private static final DecimalFormat RELEVANCE_FORMATTER = new DecimalFormat("0.00000");
    private final SiteService siteService;

    @Override
    public BaseResponse getSearchResult(SearchOptions searchOptions) {
        String query = searchOptions.query();
        if (query == null) {
            throw new QueryParamError(ErrorMessage.QUERY_IS_EMPTY.getMessage());
        }

        int limit = searchOptions.limit();
        int offset = searchOptions.offset();
        SearchResponse searchResponse = new SearchResponse();
        List<SearchInfo> data = searchResponse.getData();
        List<SiteEntity> sites = getSites(searchOptions.site());

        if (sites.isEmpty()) {
            throw new SiteOutOfBoundConfigFile(ErrorMessage.SITE_OUT_OF_BOUND_CONFIG_FILE.getMessage());
        }

        for (SiteEntity site : sites) {
            if (!site.getStatus().equals(SearchStatus.INDEXED)) {
                throw new SiteSearchStatusError(ErrorMessage.SEARCH_STATUS.getMessage());
            }

            List<LemmaEntity> lemmas = getLemmas(searchOptions.query(), site.getId());
            List<PageEntity> pages = getPagesByLemmas(lemmas);

            for (PageEntity page : pages) {
                SearchInfo searchInfo = new SearchInfo();
                searchInfo.setSite(site.getUrl());
                searchInfo.setSiteName(site.getName());
                searchInfo.setUri(page.getPath());
                searchInfo.setTitle(siteService.getPageTitle(page));
                searchInfo.setSnippet(siteService.getSnippet(page, searchOptions.query()));
                searchInfo.setRelevance(siteService.getPageRelevance(page));
                data.add(searchInfo);
            }
        }

        if (!data.isEmpty()) {
            List<SearchInfo> searchInfo = getSearchInfoResults(data, limit, offset);
            searchResponse.setData(searchInfo);
        }

        searchResponse.setCount(data.size());
        searchResponse.setResult(true);

        return searchResponse;
    }

    private List<SearchInfo> getSearchInfoResults(List<SearchInfo> data, int limit, int offset) {
        List<SearchInfo> result = new ArrayList<>();
        sortByRelativeRelevance(data);

        for (int i = offset; i <= limit; i++) {
            result.add(data.get(i));
        }

        return result;
    }

    private void sortByRelativeRelevance(List<SearchInfo> data) {
        Float maxRelevance = data.stream()
                .max((o1, o2) -> Float.compare(o1.getRelevance(), o2.getRelevance()))
                .get().getRelevance();

        for (SearchInfo item : data) {
            float relativeRelevance = item.getRelevance() / maxRelevance;
            relativeRelevance = Float.parseFloat(RELEVANCE_FORMATTER.format(relativeRelevance));
            item.setRelevance(relativeRelevance);
        }

        Collections.sort(data);
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

    private List<LemmaEntity> getLemmas(String query, Integer siteId) {
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            List<String> lemmas = lemmaFinder.getLemmasFromText(query);
            List<LemmaEntity> lemmaEntities = siteService.findLemmasBySiteId(lemmas, siteId);

            return lemmaEntities.stream()
                    .filter(lemma -> lemma.getFrequency() < MAX_LEMMA_FREQUENCY)
                    .sorted(Comparator.comparingInt(LemmaEntity::getFrequency))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<PageEntity> getPagesByLemmas(List<LemmaEntity> lemmas) {
        List<PageEntity> pages = new ArrayList<>();
        if (lemmas.isEmpty()) {
            return pages;
        }

        pages = siteService.getPagesByLemma(lemmas.get(0));

        for (int i = 1; i < lemmas.size(); i++) {
            pages = getPagesContainsLemma(lemmas.get(i), pages);
        }

        return pages;
    }

    private List<PageEntity> getPagesContainsLemma(LemmaEntity lemma, List<PageEntity> pages) {
        List<PageEntity> resultList = new ArrayList<>();
        for (PageEntity page : pages) {
            if (isPageContainsLemma(page, lemma)) {
                resultList.add(page);
            }
        }

        return resultList;
    }

    private boolean isPageContainsLemma(PageEntity page, LemmaEntity lemma) {
        List<IndexEntity> indexes = page.getIndexes();
        for (IndexEntity index : indexes) {
            if (index.getLemma().getId().equals(lemma.getId())) {
                return true;
            }
        }

        return false;
    }
}
