package searchengine.services;

import org.jsoup.Connection;
import searchengine.config.Site;
import searchengine.dto.model.LemmasDto;
import searchengine.dto.model.PageDto;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface SiteService {
    List<Site> getSitesFromConfig();

    PageEntity createPageEntity(PageDto pageDto, SiteEntity site, String url);

    List<LemmaEntity> createLemmaEntities(LemmasDto lemmasDto, SiteEntity site);

    List<IndexEntity> createIndexes(PageEntity page, SiteEntity site, LemmasDto lemmasDto);

    SiteEntity saveSite(String url, String name);

    PageEntity savePage(PageEntity page);

    void saveAllPages(List<PageEntity> pages);

    void saveLemmas(List<LemmaEntity> lemmaEntities, SiteEntity site);

    void saveIndexes(List<IndexEntity> indexes);

    void deleteSiteByUrl(String url);

    void deletePageById(Integer id);

    Iterable<SiteEntity> findAllSites();

    Optional<SiteEntity> findSiteByUrl(String url);

    Optional<PageEntity> findPageByUrl(SiteEntity site, String url);

    void updateStatusTime(LocalDateTime localDateTime, Integer id);

    void updateLastError(String error, Integer id);

    void updateSearchStatus(String status, Integer id);

    void updateSitesInDatabaseFromConfigFile();

    int getTotalPagesCount();

    int getPagesCountBySiteId(Integer id);

    int getTotalLemmasCount();

    int getLemmasCountBySiteId(Integer id);

    Connection.Response getResponse(String url) throws IOException;
}
