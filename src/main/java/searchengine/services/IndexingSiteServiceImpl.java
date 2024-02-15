package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.dto.BaseResponse;
import searchengine.dto.model.LemmasDto;
import searchengine.dto.model.PageDto;
import searchengine.exceptions.ErrorMessage;
import searchengine.exceptions.IncorrectMethodCallException;
import searchengine.exceptions.SiteOutOfBoundConfigFile;
import searchengine.model.*;
import searchengine.utils.LemmaFinder;
import searchengine.utils.PageWalker;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingSiteServiceImpl implements IndexingSiteService {
    private final SiteService siteService;
    private ForkJoinPool forkJoinPool;
    private ExecutorService executorService;
    private boolean indexing = false;

    @Override
    public BaseResponse startIndexing() {
        if (getIndexing()) {
            throw new IncorrectMethodCallException(ErrorMessage.START_INDEXING.getMessage());
        }
        List<Site> sites = siteService.getSitesFromConfig();
        prepareForIndexing(sites.size());
        for (Site site : sites) {
            siteService.deleteSiteByUrl(site.getUrl());
            SiteEntity siteEntity = siteService.saveSite(site.getUrl(), site.getName());
            walkAndIndexingPages(siteEntity);
        }
        BaseResponse response = new BaseResponse();
        response.setResult(true);

        return response;
    }

    @Override
    public BaseResponse stopIndexing() {
        if (!getIndexing()) {
            throw new IncorrectMethodCallException(ErrorMessage.STOP_INDEXING.getMessage());
        }
        forkJoinPool.shutdownNow();
        BaseResponse response = new BaseResponse();
        response.setResult(true);

        return response;
    }

    @Override
    public BaseResponse pageIndexing(String url) {
        siteService.updateSitesInDatabaseFromConfigFile();
        Optional<SiteEntity> maybeSite = siteService.findSiteByUrl(url);
        if (maybeSite.isEmpty()) {
            throw new SiteOutOfBoundConfigFile(ErrorMessage.SITE_OUT_OF_BOUND_CONFIG_FILE.getMessage());
        }
        SiteEntity site = maybeSite.get();
        Optional<PageEntity> maybePage = siteService.findPageByUrl(site, url);
        maybePage.ifPresent(page -> siteService.deletePageById(page.getId()));
        indexing = true;
        try {
            Connection.Response response = siteService.getResponse(url);
            int statusCode = response.statusCode();
            Document document = response.parse();
            PageDto pageDto = new PageDto(statusCode, document.html());
            createIndex(site, url, pageDto);
            siteService.updateSearchStatus(SearchStatus.INDEXED.name(), site.getId());
        } catch (IOException e) {
            siteService.updateLastError(ErrorMessage.CONNECT_ERROR.getMessage() + site.getUrl(), site.getId());
        }
        indexing = false;
        BaseResponse response = new BaseResponse();
        response.setResult(true);

        return response;
    }

    @Override
    public void createIndex(SiteEntity site, String url, PageDto pageDto) {
        siteService.updateStatusTime(LocalDateTime.now(), site.getId());
        try {
            PageEntity pageEntity = siteService.createPageEntity(pageDto, site, url);
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            LemmasDto lemmas = new LemmasDto(lemmaFinder.getLemmasFromHtmlPage(pageEntity.getContent()));
            List<LemmaEntity> lemmaEntities = siteService.createLemmaEntities(lemmas, site);
            if (!lemmaEntities.isEmpty()) {
                siteService.savePage(pageEntity);
                siteService.saveLemmas(lemmaEntities, site);
                List<IndexEntity> indexes = siteService.createIndexes(pageEntity, lemmaEntities, lemmas);
                siteService.saveIndexes(indexes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareForIndexing(int size) {
        forkJoinPool = new ForkJoinPool();
        executorService = Executors.newFixedThreadPool(size);

        Thread checkIndexingStatus = new Thread(() -> {
            while (true) {
                if(executorService.isTerminated() || forkJoinPool.isShutdown()) {
                    indexing = false;
                    break;
                }
            }
        });

        indexing = true;
        checkIndexingStatus.start();
    }

    private void walkAndIndexingPages(SiteEntity site) {
        PageWalker pageWalker = new PageWalker(site.getUrl(), site, siteService, this);
        executorService.execute(() -> {
            try {
                forkJoinPool.invoke(pageWalker);
                siteService.updateSearchStatus(SearchStatus.INDEXED.name(), site.getId());
            } catch (RuntimeException e) {
                siteService.updateSearchStatus(SearchStatus.FAILED.name(), site.getId());
                throw new RuntimeException(e);
            } finally {
                executorService.shutdown();
            }
        });
    }

    public boolean getIndexing() {
        return indexing;
    }
}
