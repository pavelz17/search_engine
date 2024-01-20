package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConnection;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.BaseResponse;
import searchengine.exceptions.IncorrectMethodCallException;
import searchengine.model.SearchStatus;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.PageWalker;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingSiteServiceImpl implements IndexingSiteService {
    private static final String START_INDEXING_ERROR_MESSAGE = "Индексация уже запущена";
    private static final String STOP_INDEXING_ERROR_MESSAGE = "Индексация не запущена";
    private final SitesList sites;
    private final JsoupConnection jsoupConnection;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private ForkJoinPool forkJoinPool;
    private ExecutorService executorService;
    private boolean indexingRun = false;
    private Thread checkIndexingRun;

    @Override
    public BaseResponse startIndexing() {
        if (isIndexingRun()) {
            throw new IncorrectMethodCallException(START_INDEXING_ERROR_MESSAGE);
        }
        initThreads();
        indexingRun = true;
        checkIndexingRun.start();
        for (Site site : sites.getSites()) {
            deleteSiteDataFromDatabase(site);
            SiteEntity siteEntity = saveSiteEntityToDatabase(site);
            walk(siteEntity);
        }
        BaseResponse response = new BaseResponse();
        response.setResult(true);
        return response;
    }

    @Override
    public BaseResponse stopIndexing() {
        if (!isIndexingRun()) {
            throw new IncorrectMethodCallException(STOP_INDEXING_ERROR_MESSAGE);
        }
        forkJoinPool.shutdownNow();
        BaseResponse response = new BaseResponse();
        response.setResult(true);
        return response;
    }

    private boolean isIndexingRun() {
        return indexingRun;
    }

    private void initThreads() {
        forkJoinPool = new ForkJoinPool();
        executorService = Executors.newFixedThreadPool(sites.getSites().size());
        checkIndexingRun = new Thread(() -> {
            long start = System.currentTimeMillis();
            while (true) {
                if(executorService.isTerminated() || forkJoinPool.isShutdown()) {
                    indexingRun = false;
                    break;
                }
            }
            long end = (System.currentTimeMillis() - start)/100;
            long min = end / 60;
            long sec = end % 60;
            System.out.printf("%d min %d sec", min, sec);
        });
    }

    private void deleteSiteDataFromDatabase(Site site) {
        siteRepository.deleteByUrl(site.getUrl());
    }

    private SiteEntity saveSiteEntityToDatabase(Site site) {
        String siteUrl = site.getUrl();
        siteUrl = siteUrl.endsWith("/") ? siteUrl : siteUrl.concat("/");
        SiteEntity siteEntity = SiteEntity.builder()
                .status(SearchStatus.INDEXING)
                .statusTime(LocalDateTime.now())
                .name(site.getName())
                .url(siteUrl)
                .build();
        return siteRepository.save(siteEntity);
    }

    private void walk(SiteEntity siteEntity) {
        PageWalker walker = new PageWalker(siteEntity, siteRepository, jsoupConnection, siteEntity.getUrl());
        executorService.execute(() -> {
            try {
                forkJoinPool.invoke(walker);
                siteRepository.updateSearchStatus(SearchStatus.INDEXED.name(), siteEntity.getId());
                pageRepository.saveAll(siteEntity.getPages());
            } catch (RuntimeException e) {
                siteRepository.updateSearchStatus(SearchStatus.FAILED.name(), siteEntity.getId());
                pageRepository.saveAll(siteEntity.getPages());
            } finally {
                executorService.shutdown();
            }
        });
    }
}
