package searchengine.services;

import org.jsoup.Connection;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;


public interface SiteService {
    SiteEntity saveSite(String url, String name);

    PageEntity savePage(SiteEntity site, String url, Connection.Response response) throws IOException;

    void saveLemmas(SiteEntity site, Map<String, Integer> lemmas);

    void saveIndexes(SiteEntity site, PageEntity page);

    void deleteSiteByUrl(String url);

    void deletePageById(Integer id);

    Optional<SiteEntity> findSiteByUrl(String url);

    Optional<PageEntity> findPageByPath(String path);

    void walkAndSavePages(SiteEntity site, ExecutorService executorService, ForkJoinPool forkJoinPool);

    void updateStatusTime(LocalDateTime localDateTime, Integer id);

    void updateLastError(String error, Integer id);

    Connection.Response getResponse(String url) throws IOException;
}
