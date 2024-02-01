package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.*;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteService siteService;
    private final IndexingSiteService indexingSiteService;


    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(siteService.getSitesFromConfig().size());
        total.setPages(siteService.getTotalPagesCount());
        total.setLemmas(siteService.getTotalLemmasCount());
        total.setIndexing(indexingSiteService.getIndexing());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        Iterable<SiteEntity> sites = siteService.findAllSites();

        for (SiteEntity site : sites) {
            DetailedStatisticsItem item;
            if (site.getLastError() == null) {
                item = createDetailResponse(site);
            } else {
                item = createDetailResponseWithError(site);
            }
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private DetailedStatisticsItem createDetailResponse(SiteEntity site) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setUrl(site.getUrl());
        item.setName(site.getName());
        item.setStatus(site.getStatus().name());
        item.setPages(siteService.getPagesCountBySiteId(site.getId()));
        item.setLemmas(siteService.getLemmasCountBySiteId(site.getId()));
        return item;
    }

    private DetailedStatisticsItem createDetailResponseWithError(SiteEntity site) {
        DetailedStatisticsItemWithError item = new DetailedStatisticsItemWithError();
        item.setUrl(site.getUrl());
        item.setName(site.getName());
        item.setStatus(site.getStatus().name());
        item.setPages(siteService.getPagesCountBySiteId(site.getId()));
        item.setLemmas(siteService.getLemmasCountBySiteId(site.getId()));
        item.setError(site.getLastError());
        return item;
    }
}
