package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.dto.BaseResponse;
import searchengine.exceptions.IncorrectMethodCallException;
import searchengine.exceptions.SiteOutOfBoundConfigFile;
import searchengine.model.PageEntity;
import searchengine.model.SearchStatus;
import searchengine.model.SiteEntity;
import searchengine.utils.LemmaFinder;
import searchengine.utils.PageWalker;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingSiteServiceImpl implements IndexingSiteService {
    private static final String START_INDEXING_ERROR_MESSAGE = "Индексация уже запущена";
    private static final String STOP_INDEXING_ERROR_MESSAGE = "Индексация не запущена";
    private static final String SITE_OUT_OF_BOUND_ERROR_MESSAGE = "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";
    private static final String CONNECT_ERROR_MESSAGE = "Не удалось подключиться к странице: ";
    private final SiteService siteService;
    private ForkJoinPool forkJoinPool;
    private ExecutorService executorService;
    private boolean indexing = false;

    @Override
    public BaseResponse startIndexing() {
        if (getIndexing()) {
            throw new IncorrectMethodCallException(START_INDEXING_ERROR_MESSAGE);
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
            throw new IncorrectMethodCallException(STOP_INDEXING_ERROR_MESSAGE);
        }
        forkJoinPool.shutdownNow();
        BaseResponse response = new BaseResponse();
        response.setResult(true);
        return response;
    }

    @Override
    public BaseResponse pageIndexing(String url) {
        siteService.updateSitesFromConfigFile();
        Optional<SiteEntity> maybeSite = siteService.findSiteByUrl(url);
        if (maybeSite.isEmpty()) {
            throw new SiteOutOfBoundConfigFile(SITE_OUT_OF_BOUND_ERROR_MESSAGE);
        }
        SiteEntity site = maybeSite.get();
        Optional<PageEntity> maybePage = siteService.findPageByUrl(site, url);
        maybePage.ifPresent(page -> siteService.deletePageById(page.getId()));
        try {
            Connection.Response response = siteService.getResponse(url);
            int statusCode = response.statusCode();
            Document document = response.parse();
            String path = url.replace(site.getUrl(), "/");
            PageEntity page = buildPage(statusCode, path, document.html(), site);
            createIndex(site, page);
            siteService.updateSearchStatus(SearchStatus.INDEXED.name(), site.getId());
        } catch (IOException e) {
            siteService.updateLastError(CONNECT_ERROR_MESSAGE + site.getUrl(), site.getId());
        }
        BaseResponse response = new BaseResponse();
        response.setResult(true);
        return response;
    }

    @Override
    public void createIndex(SiteEntity site, PageEntity page) {
        siteService.updateStatusTime(LocalDateTime.now(), site.getId());
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            HashMap<String, Integer> lemmas = lemmaFinder.getLemmas(page.getContent());
            siteService.savePage(page);
            siteService.saveLemmasAndIndexes(lemmas, site, page);
        } catch (IOException e) {
            System.out.println("LemmaFinder throw exception");
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
                pageWalker.setRunning(true);
                forkJoinPool.invoke(pageWalker);
                siteService.updateSearchStatus(SearchStatus.INDEXED.name(), site.getId());
            } catch (RuntimeException e) {
                siteService.updateSearchStatus(SearchStatus.FAILED.name(), site.getId());
            } finally {
                pageWalker.setRunning(false);
                executorService.shutdown();
            }
        });
    }
    private PageEntity buildPage(Integer statusCode, String path, String html, SiteEntity site) {
        return PageEntity.builder()
                .code(statusCode)
                .path(path)
                .content(html)
                .site(site)
                .build();
    }

    public boolean getIndexing() {
        return indexing;
    }
}
