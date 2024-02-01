package searchengine.services;

import org.jsoup.Connection;
import searchengine.config.Site;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


public interface SiteService {
    List<Site> getSitesFromConfig();

    void updateSitesFromConfigFile();

    SiteEntity saveSite(String url, String name);

    PageEntity savePage(PageEntity page);

    void saveLemmasAndIndexes(HashMap<String, Integer> lemmas, SiteEntity site, PageEntity page);

    void deleteSiteByUrl(String url);

    void deletePageById(Integer id);

    Iterable<SiteEntity> findAllSites();

    Optional<SiteEntity> findSiteByUrl(String url);

    Optional<PageEntity> findPageByUrl(SiteEntity site, String url);

    void updateStatusTime(LocalDateTime localDateTime, Integer id);

    void updateLastError(String error, Integer id);

    void updateSearchStatus(String status, Integer id);

    Connection.Response getResponse(String url) throws IOException;

    void saveAllPages(List<PageEntity> pages);

    int getTotalPagesCount();

    int getTotalLemmasCount();

    int getPagesCountBySiteId(Integer id);

    int getLemmasCountBySiteId(Integer id);
}
